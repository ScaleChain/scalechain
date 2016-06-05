package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.{ChainException, ErrorCode, GeneralException}
import io.scalechain.blockchain.chain.mining.BlockTemplate
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.{BlockInfoFactory, BlockStorage, Storage, DiskBlockStorage, GenesisBlock}
import io.scalechain.blockchain.storage

import io.scalechain.blockchain.chain.mempool.{TransactionMempool, TransientTransactionStorage}
import io.scalechain.blockchain.transaction._
import io.scalechain.util.Utils
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.TailCalls.TailRec


object Blockchain {
  var theBlockchain : Blockchain = null
  def create(storage : BlockStorage) = {
    theBlockchain = new Blockchain(storage)

    // Load any in memory structur required by the Blockchain class from the on-disk storage.
    new BlockchainLoader(theBlockchain, storage).load()
    theBlockchain
  }
  def get() = {
    assert( theBlockchain != null)
    theBlockchain
  }
}


class BlockchainLoader(chain:Blockchain, storage : BlockStorage) {

  /** Starting from the best block, crawl up to the genesis block to create the list of block hashes.
    * This is necessary, as we need to calculate chainwork from the genesis block to the best block,
    * but the BlockStorage provides the backward link from a block to the previous block of it.
    *
    */
  @tailrec
  final protected[chain] def getBlockHashList(currentBlockHash : Hash, blockHashList : List[Hash]): List[Hash] = {
    val env = ChainEnvironment.get
    if ( currentBlockHash.value == env.GenesisBlockHash.value ) { // The base case. We reached from the best block to the genesis block.
      currentBlockHash :: blockHashList
    } else { // We have more blocks to back-track.
      val blockInfo = storage.getBlockInfo(Hash( currentBlockHash.value) )
      assert(blockInfo.isDefined)

      getBlockHashList(blockInfo.get.blockHeader.hashPrevBlock, currentBlockHash :: blockHashList)
    }
  }

  def load() : Unit = {
    val bestBlockHashOption = storage.getBestBlockHash()
    if (bestBlockHashOption.isDefined) {
      // Set the best block descriptor.
      chain.theBestBlock  = storage.getBlockInfo(bestBlockHashOption.get).get
    } else {
      // We don't have the best block hash yet.
      // This means that we did not put the genesis block yet.
      // On the CLI layer, while initializing all layers, the genesis block will be put, so we do nothing here.
    }
  }
}

/** Maintains the best blockchain, whose chain work is the biggest one.
  *
  * The block metadata is kept in a tree data structure on-disk.
  * The actual block data is also kept on-disk.
  *
  * [ Overview ]
  *
  * The chain work for a block is the total number of hash calculations from block 0 to the current best block.
  *
  * For example, if we calculated hashes 10, 20, 15 times for three blocks B0, B1, and B2, the chain work is 45(10+20+15).
  *
  *   B0(10) → B1(10+20) → B2(10+20+15) : The best chain.
  *
  * Based on the total chain work of the new block, we decide the best blockchain.
  * For example, if we found a block B2' whose chain work(50) is greater than the current maxium(45),
  * we will keep B2' as the best block and update the best blockchain.
  *
  *   B0(10) → B1(10+20) → B2'(10+20+20) : The best chain.
  *                      ↘ B2(10+20+15) : This is a fork.
  *
  * When a new block B3 is added to the blockchain, we will add it on top of the best blockchain.
  *
  *   B0 → B1 → B2' → B3 : The best chain.
  *           ↘ B2
  *
  * Only transactions in the best blockchain remain effective.
  * Because B2 remains in a fork, all transactions in B2 are migrated to the disk-pool, except ones that are included in B3.
  *
  * The disk-pool is where transactions that are not in any block of the best blockchain are stored.
  * ( Bitcoin core stores transactions in memory using mempool, but ScaleChain stores transactions on-disk using disk-pool ;-). )
  * TransactionDescriptor can either store record location of the transaction if the transaction was written as part of a block on disk.
  * Otherwise, TransactionDescriptor can stores a serialized transaction, and TransactionDescriptor itself is stored as a value of RocksDB keyed by the transaction hash.
  *
  * Of course, block a reorganization can invalidate more than two blocks at once.
  *
  * Time T :
  *   B0(10) → B1(30) → B2(45) : The best chain.
  *
  * Time T+1 : Add B1' (chain work = 35)
  *   B0(10) → B1(30) → B2(45) : The best chain.
  *          ↘ B1'(35)
  *
  * Time T+2 : Add B2' (chain work = 55)
  *   B0(10) → B1(30) → B2(45)
  *          ↘ B1'(35) -> B2'(55) : The best chain.
  *
  * In this case all transactions in B1, B2 but not in B1' and B2' are moved to the disk-pool so that they can be added to
  * the block chain later when a new block is created.
  *
  */
class Blockchain(storage : BlockStorage) extends BlockchainView with ChainConstraints {
  private val logger = LoggerFactory.getLogger(classOf[Blockchain])

  var chainEventListener : Option[ChainEventListener] = None

  /** Set an event listener of the blockchain.
    *
    * @param listener The listener that wants to be notified for new blocks, invalidated blocks, and transactions comes into and goes out from the transaction mempool..
    */
  def setEventListener( listener : ChainEventListener ): Unit = {
    chainEventListener = Some(listener)
  }

  /** The descriptor of the best block.
    * This value is updated whenever a new best block is found.
    * We also have to check if we need to do block reorganization whenever this field is updated.
    */
  var theBestBlock : BlockInfo = null

  /** Put a block onto the blockchain.
    *
    * (1) During initialization, we call putBlock for each block we received until now.
    * (2) During IBD(Initial Block Download), we call putBlock for blocks we downloaded.
    * (3) When a new block was received from a peer.
    *
    * Caller of this method should check if the bestBlock was changed.
    * If changed, we need to update the best block on the storage layer.
    *
    * TODO : Need to check the merkle root hash in the block.
    *
    * @param block The block to put into the blockchain.
    */
  def putBlock(blockHash : Hash, block:Block) : Unit = {
    synchronized {
      if (storage.hasBlock(blockHash)) {
        logger.info(s"Duplicate block was ignored. Block hash : ${blockHash}")
      } else {

        // Case 1. If it is the genesis block, set the genesis block as the current best block.
        if (block.header.hashPrevBlock.isAllZero()) {
          assert(theBestBlock == null)
          storage.putBlock(block)
          storage.putBlockHashByHeight(0, blockHash)
          theBestBlock = storage.getBlockInfo(blockHash).get
          chainEventListener.map(_.onNewBlock(ChainBlock( height = 0, block)))
        } else { // Case 2. Not a genesis block.
          assert(theBestBlock != null)

          // Step 2.1 : Get the block descriptor of the previous block.
          val prevBlockDesc: Option[BlockInfo] = storage.getBlockInfo(block.header.hashPrevBlock)
          // We already checked if the parent block exists.
          assert(prevBlockDesc.isDefined)

          // Case 2.A : The previous block of the new block is the current best block.
          if (prevBlockDesc.get.blockHeader.hash.value == theBestBlock.blockHeader.hash.value) {
            // Step 2.A.1 : Update the best block
            storage.putBlock(block)
            val blockInfo = storage.getBlockInfo(blockHash).get
            storage.putBlockHashByHeight(blockInfo.height, blockHash)

            theBestBlock = blockInfo

            // TODO : Update best block in wallet (so we can detect restored wallets)

            // Step 2.A.2 : Remove transactions in the block from the disk-pool.
            // TODO : Implement.
            assert(false)
            /*
            block.transactions.foreach { transaction =>
              mempool.del(transaction.hash)
            }
            */

            chainEventListener.map(_.onNewBlock(ChainBlock(height = blockInfo.height, block)))

            logger.info(s"Successfully have put the block in the best blockchain.\n Hash : ${blockHash}")

          } else { // Case 2.B : The previous block of the new block is NOT the current best block.
            storage.putBlock(block)
            val blockInfo = storage.getBlockInfo(blockHash).get
            theBestBlock = blockInfo

            // Step 3.B.1 : See if the chain work of the new block is greater than the best one.
            if (blockInfo.chainWork > theBestBlock.chainWork) {
              logger.warn("Block reorganization started.")

              // Step 3.B.2 : Reorganize the blocks.
              // transaction handling, orphan block handling is done in this method.
              reorganize(originalBestBlock = theBestBlock, newBestBlock = blockInfo)

              // Step 3.B.3 : Update the best block
              theBestBlock = blockInfo

              // TODO : Update best block in wallet (so we can detect restored wallets)
            }
          }
        }
      }
    }
  }

  /** Put a transaction we received from peers into the mempool.
    *
    * @param transaction The transaction to put into the mempool.
    */
  def putTransaction(transaction : Transaction) : Unit = {
    synchronized {
      val txHash = transaction.hash
      // TODO : Implement using disk-pool, instead of mempool.
      assert(false)
      /*
      if ( mempool.exists(txHash)) {
        logger.info(s"A duplicate transaction in the mempool was discarded. Hash : ${txHash}")
      } else {
        mempool.put(transaction)
        chainEventListener.map(_.onNewTransaction(transaction))

        logger.info(s"A new transaction was put into mempool. Hash : ${txHash}")
      }&/
    }
  }


  /** Calculate the (encoded) difficulty bits that should be in the block header.
    *
    * @param prevBlockDesc The descriptor of the previous block. This method calculates the difficulty of the next block of the previous block.
    * @return
    */
  def calculateDifficulty(prevBlockDesc : BlockInfo) : Long = {
    if (prevBlockDesc.height == 0) { // The genesis block
      GenesisBlock.BLOCK.header.target
    } else {
      // BUGBUG : Make sure that the difficulty calculation is same to the one in the Bitcoin reference implementation.
      val currentBlockHeight = prevBlockDesc.height + 1
      if (currentBlockHeight % 2016 == 0) {
        // TODO : Calculate the new difficulty bits.
        assert(false)
        -1L
      } else {
        prevBlockDesc.blockHeader.target
      }
    }
  }

  /** Get the template for creating a block containing a list of transactions.
    *
    * @return The block template which has a sorted list of transactions to include into a block.
    */
  def getBlockTemplate(coinbaseData : CoinbaseData, minerAddress : CoinAddress) : BlockTemplate = {
    // TODO : P1 - Use difficulty bits
    //val difficultyBits = getDifficulty()
    val difficultyBits = 10

    val validTransactions = mempool.getValidTransactions()
    // Select transactions by priority and fee. Also, sort them.
    val sortedTransactions = selectTransactions(validTransactions, Block.MAX_SIZE)
    val generationTranasction =
      TransactionBuilder.newBuilder(this)
      .addGenerationInput(coinbaseData)
      .addOutput(CoinAmount(50), minerAddress)
      .build()

    new BlockTemplate(difficultyBits, generationTranasction :: sortedTransactions)
  }


  /** Select transactions to include into a block.
    *
    *  Order transactions by fee in descending order.
    *  List N transactions based on the priority and fee so that the serialzied size of block
    *  does not exceed the max size. (ex> 1MB)
    *
    *  <Called by>
    *  When a miner tries to create a block, we have to create a block template first.
    *  The block template has the transactions to keep in the block.
    *  In the block template, it has all fields set except the nonce and the timestamp.
    *
    * @param transactions The candidate transactions
    * @param maxBlockSize The maximum block size. The serialized block size including the block header and transactions should not exceed the size.
    * @return The transactions to put into a block.
    */
  protected def selectTransactions(transactions : Iterator[Transaction], maxBlockSize : Int) : List[Transaction] = {
    // Step 1 : TODO : Select high priority transactions


    // Step 2 : TODO : Sort transactions by fee in descending order.

    // Step 3 : TODO : Choose transactions until we fill up the max block size.

    // Step 4 : TODO : Sort transactions based on a criteria to store into a block.

    // TODO : Need to check the sort order of transactions in a block.
    transactions.toList
  }

  /** Reorganize blocks.
    * This method is called when the new best block is not based on the original best block.
    *
    * @param originalBestBlock The original best block before the new best one was found.
    * @param newBestBlock The new best block, which has greater chain work than the original best block.
    */
  protected def reorganize(originalBestBlock : BlockInfo, newBestBlock : BlockInfo) : Unit = {
    assert( originalBestBlock.chainWork < newBestBlock.chainWork)

    // Step 1 : find the common ancestor of the two blockchains.
    val commonBlock : BlockInfo = findCommonBlock(originalBestBlock, newBestBlock)

    // TODO : Call chainEventListener : onNewBlock, onRemoveBlock

    /*

      // Step 1 : Find the common block(pfork) between the current blockchain(pindexBest) and the new longer blockchain.

      // Step 2 : Get the list of blocks to disconnect from the common block to the tip of the current blockchain

      // Step 3 : Get the list of blocks to connect from the common block to the longer blockchain.

      // Step 4 : Reorder the list of blocks to connect so that the blocks with lower height come first.

      // Step 5 : Disconnect blocks from the current (shorter) blockchain. (order : newest to oldest)
      LOOP block := For each block to disconnect
          // Step 5.1 : Read each block, and disconnect each block.
          block.ReadFromDisk(pindex)
          block.DisconnectBlock(txdb, pindex)
              1. Mark all outputs spent by all inputs of all transactions in the block as unspent.
              LOOP tx := For each transaction in the block
                  1.1. Mark outputs pointed by the inputs of the transaction unspent.
                  tx.DisconnectInputs
                      - LOOP input := For each input in the transaction
                          - Get the transaction pointed by the input
                          - On disk, Mark the output point by the input as spent
              2. On disk, disconnect from the previous block
                  (previous block.next = null)

          // Step 5.2 : Prepare transactions to add back to the mempool.
      }

      // Step 6 : Connect blocks from the longer blockchain to the current blockchain. (order : oldest to newest)
      LOOP block := For each block to connect
          // kangmo : comment - Step 6.1 : Read block, connect the block to the current blockchain, which does not have the disconnected blocks.
          block.ReadFromDisk(pindex)
          block.ConnectBlock(txdb, pindex)
              - 1. Do preliminary checks for a block
              pblock->CheckBlock()

              - 2. Prepare a queue for database changes marking outputs spent by all inputs of all transactions in the block.
              map<uint256, CTxIndex> mapQueuedChanges;

              - 3. Populate mapQueuedChanges with transaction outputs marking which transactions are spending each of the outputs.
              LOOP tx := For each transaction in the block
                  - 3.1 Mark transaction outputs pointed by inputs of this transaction spent.
                  IF not coinbase transaction
                      CTransaction::ConnectInputs( .. & mapQueuedChanges .. )
                          LOOP input := for each input in the transaction
                              // 1. read CTxIndex from disk if not read yet.
                              // 2. read the transaction that the outpoint points from disk if not read yet.
                              // 3. Increase DoS score if an invalid output index was found in a transaction input.
                              // 4. check coinbase maturity for outpoints spent by a transaction.
                              // 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
                              // 6. check double spends for each OutPoint of each transaction input in a transaction.
                              // 7. check value range of each input and sum of inputs.
                              // 8. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output.

                          // check if the sum of input values is greater than or equal to the sum of outputs.
                          // make sure if the fee is not negative.
                          // check the minimum transaction fee for each transaction.
                  // Add UTXO : set all outputs are unspent for the newly connected transaction.

              - 4. For each items in mapQueuedChanges, write to disk.
              - 5. Check if the generation transaction's output amount is less than or equal to the reward + sum of fees for all transactions in the block.
              - 6. On disk, connect the block from the previous block. (previous block.next = this block)

              - 7. For each transaction, sync with wallet.
              LOOP tx := For each transaction in the block
                  SyncWithWallets
                      - For each registered wallet
                           pwallet->AddToWalletIfInvolvingMe
          // Step 6.2 : Prepare transactions to remove from the mempool
      }

      // Step 7 : Write the hash of the tip block on the best blockchain, commit the db transaction.

      // Step 8 : Set the next block pointer for each connected block. Also, set next block pointer to null for each disconnected block.
      // Note : next pointers of in-memory block index nodes are modified after the on-disk transaction commits the on-disk version of the next pointers.

      // Step 9 : add transactions in the disconnected blocks to the mempool.

      // Step 10 : Remove transactions in the connected blocks from the mempool.
*/
  }

  /** Get the descriptor of the common ancestor of the two given blocks.
    *
    * @param block1 The first given block.
    * @param block2 The second given block.
    */
  protected def findCommonBlock(block1 : BlockInfo, block2 : BlockInfo) : BlockInfo = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Return an iterator that iterates each ChainBlock from a given height.
    *
    * Used by : importAddress RPC to rescan the blockchain.
    *
    * @param height Specifies where we start the iteration. The height 0 means the genesis block.
    * @return The iterator that iterates each ChainBlock.
    */
  def getIterator(height : Long) : Iterator[ChainBlock] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Return the block height of the best block.
    *
    * @return The best block height.
    */
  def getBestBlockHeight() : Long = {
    assert(theBestBlock != null)
    theBestBlock.height
  }

  /** Return the hash of block on the tip of the best blockchain.
    *
    * @return The best block hash.
    */
  def getBestBlockHash() : Option[Hash] = {
    storage.getBestBlockHash()
  }


  /** Get the hash of a block specified by the block height on the best blockchain.
    *
    * Used by : getblockhash RPC.
    *
    * @param blockHeight The height of the block.
    * @return The hash of the block header.
    */
  def getBlockHash(blockHeight : Long) : Hash = {
    val blockHashOption = storage.getBlockHashByHeight(blockHeight)
    // TODO : Bitcoin Compatiblity : Make the error code compatible when the block height was a wrong value.
    if (blockHashOption.isEmpty) {

      throw new ChainException( ErrorCode.InvalidBlockHeight)
    }
    blockHashOption.get
  }

  /** See if a block exists on the blockchain.
    *
    * Used by : submitblock RPC to check if a block already exists.
    *
    * @param blockHash The hash of the block header to check.
    * @return true if the block exists; false otherwise.
    */
  def hasBlock(blockHash : Hash) : Boolean = {
    storage.hasBlock(blockHash)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)] = {
    storage.getBlock(blockHash)
  }
  /** Return a transaction that matches the given transaction hash.
    *
    * Used by listtransaction RPC to get the
    *
    * @param transactionHash The transaction hash to search.
    * @return Some(transaction) if the transaction that matches the hash was found. None otherwise.
    */
  def getTransaction(transactionHash : Hash) : Option[Transaction] = {

    // Step 1 : Search mempool.
    val mempoolTransactionOption = mempool.get(transactionHash)

    // Step 2 : Search block database.
    val dbTransactionOption = storage.getTransaction( transactionHash )

    // Step 3 : TODO : Run validation.

    //BUGBUG : Transaction validation fails because the transaction hash on the outpoint does not exist.
    //mempoolTransactionOption.foreach( new TransactionVerifier(_).verify(DiskBlockStorage.get) )
    //dbTransactionOption.foreach( new TransactionVerifier(_).verify(DiskBlockStorage.get) )

    if ( mempoolTransactionOption.isDefined ) {
      assert(dbTransactionOption.isEmpty)
      Some(mempoolTransactionOption.get)
    } else if (dbTransactionOption.isDefined){
      Some(dbTransactionOption.get)
    } else {
      None
    }
  }

  /** Return a transaction output specified by a give out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  def getTransactionOutput(outPoint : OutPoint) : TransactionOutput = {
    // Coinbase outpoints should never come here
    assert( !outPoint.transactionHash.isAllZero() )

    val transaction = getTransaction(outPoint.transactionHash)
    if (transaction.isEmpty) {
      throw new ChainException(ErrorCode.InvalidOutPoint, "The transaction was not found : " + outPoint.transactionHash)
    }

    val outputs = transaction.get.outputs

    if( outPoint.outputIndex >= outputs.length) {
      throw new ChainException(ErrorCode.InvalidOutPoint, s"Invalid output index. Transaction hash : ${outPoint.transactionHash}, Output count : ${outputs.length}, Output index : ${outPoint.outputIndex}")
    }

    outputs(outPoint.outputIndex)
  }


  /** Get the current difficulty of block hash.
    *
    * @return
    */
  def getDifficulty() : Long = {
    calculateDifficulty(prevBlockDesc = theBestBlock)
  }

  /** Get the amount of reward that a minder gets from the generation input.
    *
    * @return
    */
  def getCoinbaseAmount() : CoinAmount = {
    // TODO : Implement
    CoinAmount(50)
  }
}

