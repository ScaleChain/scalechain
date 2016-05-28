package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.{ChainException, ErrorCode, GeneralException}
import io.scalechain.blockchain.chain.mining.BlockTemplate
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.{BlockStorage, Storage, DiskBlockStorage, GenesisBlock}

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
      // A list of block hashes from the genesis block to the
      val blockHashes = getBlockHashList( Hash(bestBlockHashOption.get.value), Nil )

      var height = -1L
      var previousBlockDesc : BlockDescriptor = null

      blockHashes foreach { blockHash =>
        height += 1

        val blockInfo = storage.getBlockInfo(Hash( blockHash.value) ).getOrElse {
          assert(false); null
        }

        if ( blockInfo.height != height) {
          throw new ChainException(ErrorCode.InvalidBlockHeightOnDatabase)
        }

        val currentBlockDesc = BlockDescriptor.create (
          previousBlockDesc, blockHash, blockInfo.blockHeader, blockInfo.transactionCount
        )

        //(previousBlockDesc, blockHash, block : Block)
        chain.chainIndex.putBlock(blockHash, currentBlockDesc)

        previousBlockDesc = currentBlockDesc
      }
      // Set the best block descriptor.
      chain.theBestBlock  = previousBlockDesc
    } else {
      // We don't have the best block hash yet.
      // This means that we did not put the genesis block yet.
      // On the CLI layer, while initializing all layers, the genesis block will be put, so we do nothing here.
    }
  }
}

/** Maintains the best blockchain, whose chain work is the biggest one.
  *
  * The blocks are kept in a tree data structure in memory.
  * The actual block data is not kept in memory, but the link from the child block to a parent block, as well as
  * summarizing information such as block hash and transaction count are kept.
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
  * Because B2 remains in a fork, all transactions in B2 are migrated to the mempool, except ones that are included in B3.
  *
  * The mempool is where transactions that are not in any block of the best blockchain are stored.
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
  * In this case all transactions in B1, B2 but not in B1' and B2' are moved to the mempool so that they can be added to
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
  var theBestBlock : BlockDescriptor = null

  /** The blocks whose parents were not received yet.
    */
  val orphanBlocks = new OrphanBlocks()

  /** The memory pool for transient transactions that are valid but not stored in any block.
    */
  val mempool = new TransactionMempool(storage)

  /** The in-memory index where we search blocks and transactions.
    */
  val chainIndex = new BlockchainIndex()


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

        // Step 0 : Check if the block is the genesis block
        if (block.header.hashPrevBlock.isAllZero()) {
          assert(theBestBlock == null)
          theBestBlock = BlockDescriptor.create(null, blockHash, block.header, block.transactions.length)
          storage.putBlock(block)
          chainIndex.putBlock(blockHash, theBestBlock)
          chainEventListener.map(_.onNewBlock(ChainBlock( height = 0, block)))
        } else {
          assert(theBestBlock != null)

          // Step 1 : Get the block descriptor of the previous block.
          val prevBlockDesc: Option[BlockDescriptor] = chainIndex.findBlock(block.header.hashPrevBlock)

          // Step 2 : Create a new block descriptor for the given block.
          if (prevBlockDesc.isEmpty) {
            logger.warn("An orphan block was found.")
            // The parent block was not received yet. Put it into an orphan block list.
            orphanBlocks.put(blockHash, block)

            // BUGBUG : An attacker can fill up my memory with lots of orphan blocks.
          } else {
            val blockDesc = BlockDescriptor.create(prevBlockDesc.get, blockHash, block.header, block.transactions.length)

            // Step 3 : Check if the previous block of the new block is the current best block.
            if (prevBlockDesc.get.blockHash.value == theBestBlock.blockHash.value) {
              // Step 3.A.1 : Update the best block
              theBestBlock = blockDesc
              storage.putBlock(block)
              chainIndex.putBlock(blockHash, blockDesc)


              // Step 3.A.2 : Remove transactions in the block from the mempool.
              block.transactions.foreach { transaction =>
                mempool.del(transaction.hash)
              }

              // Step 3.A.3 : Check if the new block is a parent of an orphan block.
              val orphans = orphanBlocks.findByDependency(blockHash)
              orphans.map { blocks =>
                orphanBlocks.remove(blockHash)
                blocks.get foreach { orphanBlock =>
                  // Step 6 : Recursively call putBlock to put the orphans
                  putBlock(blockHash, orphanBlock)
                }
              }

              chainEventListener.map(_.onNewBlock(ChainBlock(height = blockDesc.height, block)))

              logger.info(s"Successfully have put the block in the best blockchain.\n Hash : ${blockHash}")

            } else {
              if (blockDesc.chainWork > theBestBlock.chainWork) {
                logger.warn("Block reorganization is required.")
              }
              /*
              // Step 3.B.1 : See if the chain work of the new block is greater than the best one.
              if (blockDesc.chainWork > theBestBlock.chainWork) {
                // Step 3.B.2 : Reorganize the blocks.
                // transaction handling, orphan block handling is done in this method.
                reorganize(originalBestBlock = theBestBlock, newBestBlock = blockDesc)

                // Step 3.B.3 : Update the best block
                theBestBlock = blockDesc
              }
              */
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
      if ( mempool.exists(txHash)) {
        logger.info(s"A duplicate transaction in the mempool was discarded. Hash : ${txHash}")
      } else {
        mempool.put(transaction)
        chainEventListener.map(_.onNewTransaction(transaction))

        logger.info(s"A new transaction was put into mempool. Hash : ${txHash}")
      }
    }
  }


  /** Calculate the (encoded) difficulty bits that should be in the block header.
    *
    * @param prevBlockDesc The descriptor of the previous block. This method calculates the difficulty of the next block of the previous block.
    * @return
    */
  def calculateDifficulty(prevBlockDesc : BlockDescriptor) : Long = {
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
  protected def reorganize(originalBestBlock : BlockDescriptor, newBestBlock : BlockDescriptor) : Unit = {
    assert( originalBestBlock.chainWork < newBestBlock.chainWork)

    // Step 1 : find the common ancestor of the two blockchains.
    val commonBlock : BlockDescriptor = findCommonBlock(originalBestBlock, newBestBlock)

    // The transactions to add to the mempool. These are ones in the invalidated blocks but are not in the new blocks.
    val transactionsToAddToMempool : Seq[Transaction] = null

    // TODO : Call chainEventListener : onNewBlock, onRemoveBlock

    // Step 2 : TODO : transactionsToAddToMempool: add all transactions in (commonBlock, originalBestBlock] to transactions.

    // Step 3 : TODO : transactionsToAddToMempool: remove all transactions in (commonBlock, newBestBlock]

    // Step 4 : TODO : move transactionsToAddToMempool to mempool.

    // Step 5 : TODO : update the best block in the storage layer.


  }

  /** Get the descriptor of the common ancestor of the two given blocks.
    *
    * @param block1 The first given block.
    * @param block2 The second given block.
    */
  protected def findCommonBlock(block1 : BlockDescriptor, block2 : BlockDescriptor) : BlockDescriptor = {
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
    val foundBlockOption = chainIndex.findBlock(blockHeight)
    // TODO : Bitcoin Compatiblity : Make the error code compatible when the block height was a wrong value.
    if (foundBlockOption.isEmpty) {

      throw new ChainException( ErrorCode.InvalidBlockHeight)
    }
    foundBlockOption.get.blockHeader.hash
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


/** A block descriptor which has an fields to keep block chain metadata. The descriptor is kept in memory.
  */
object BlockDescriptor {
  /** Calculate the estimated number of hash calculations for a block.
    *
    * @param blockHash The block header hash to calculate the estimated number of hash calculations for creating the block.
    * @return The estimated number of hash calculations for the given block.
    */
  protected[chain] def getHashCalculations(blockHash : Hash) : Long = {
    // Step 2 : Calculate the (estimated) number of hash calculations based on the hash value.
    val hashValue = Utils.bytesToBigInteger(blockHash.value)
    val totalBits = 8 * 32

    scala.math.pow(2, totalBits - hashValue.bitLength()).toLong
  }

  /** Create a block descriptor.
    *
    * @param previousBlock The block descriptor of the previous block.
    * @param blockHash The hash of the current block.
    * @param blockHeader The block header.
    * @param transactionCount the number of transactions in the block.
    * @return The created block descriptor.
    */
  def create(previousBlock : BlockDescriptor, blockHash : Hash, blockHeader : BlockHeader, transactionCount : Int) : BlockDescriptor = {
    BlockDescriptor(previousBlock, blockHeader, blockHash, transactionCount)
  }
}

/** Wraps a block, maintains additional information such as chain work.
  *
  * We need to maintain the chain work(the total number of hash calculations from the genesis block up to a block) for each block.
  * Based on the chain work, we will decide the best blockchain.
  *
  * We will keep a tree of blocks by keeping the previous block in the current block.
  *
  * @param previousBlock The block descriptor of the previous block of the this block.
  * @param blockHash The hash of the block header.
  * @param blockHeader The header of the block.
  * @param transactionCount The number of transactions that the block has.
  */
case class BlockDescriptor(previousBlock : BlockDescriptor, blockHeader : BlockHeader, blockHash : Hash, transactionCount : Long) {
  /** The total number of hash calculations from the genesis block.
    */
  val chainWork : Long =
    (if (previousBlock == null) 0 else previousBlock.chainWork) +
    BlockDescriptor.getHashCalculations(blockHash)

  /** The height of the current block. The genesis block, whose previousBlock is null has height 0.
    */
  val height : Long =
    if (previousBlock == null) 0 else previousBlock.height + 1
}
