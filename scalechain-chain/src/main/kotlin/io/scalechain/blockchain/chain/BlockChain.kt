package io.scalechain.blockchain.chain

import io.scalechain.blockchain.storage.index.TransactingRocksDatabase
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.TransactionDescriptorIndex
import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.*

import io.scalechain.blockchain.transaction.*
import org.slf4j.LoggerFactory


class BlockchainLoader(private val db : KeyValueDatabase, private val chain:Blockchain, private val storage : BlockStorage) {

  fun load() : Unit {
    val bestBlockHashOption = storage.getBestBlockHash(db)
    if (bestBlockHashOption != null) {
      // Set the best block descriptor.
      chain.theBestBlock  = storage.getBlockInfo(db, bestBlockHashOption)!!
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
  * < Overview >
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
  * When a block B3 is added to the blockchain, we will add it on top of the best blockchain.
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
  * the block chain later when a block is created.
  *
  */
class Blockchain(val db : KeyValueDatabase, private val storage : BlockStorage) : BlockchainView {
  private val logger = LoggerFactory.getLogger(Blockchain::class.java)

  val txMagnet = TransactionMagnet(storage, txPoolIndex = storage, txTimeIndex = storage)
  val txPool = TransactionPool(storage, txMagnet)
  val blockMagnet = BlockMagnet(storage, txPool, txMagnet)

  val blockOrphanage = BlockOrphanage(storage)
  val txOrphanage = TransactionOrphanage(storage)

  fun txDescIndex() : TransactionDescriptorIndex = storage

  fun<T> withTransaction( block : (KeyValueDatabase) -> T ) : T {
    val transactingRocksDB = db.transacting()

    transactingRocksDB.beginTransaction()

    val returnValue =
      try {
        block(transactingRocksDB)
      } catch ( t : Throwable ) {
        transactingRocksDB.abortTransaction()
        throw t
      }

    transactingRocksDB.commitTransaction()
    return returnValue
  }


  /** Set an event listener of the blockchain.
    *
    * @param listener The listener that wants to be notified for new blocks, invalidated blocks, and transactions comes into and goes out from the transaction pool.
    */
  fun setEventListener( listener : ChainEventListener ): Unit {
    txMagnet.setEventListener(listener)
  }

  /** The descriptor of the best block.
    * This value is updated whenever a new best block is found.
    * We also have to check if we need to do block reorganization whenever this field is updated.
    */
  var theBestBlock : BlockInfo? = null

  /**
    * Put the best block hash into on-disk storage, as well as the in-memory best block info.
    *
    * @param blockHash
    * @param blockInfo
    */
  protected fun setBestBlock(db : KeyValueDatabase, blockHash : Hash, blockInfo : BlockInfo) : Unit {
    theBestBlock = blockInfo
    storage.putBestBlockHash(db, blockHash)
  }

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
    * @return true if the newly accepted block became the new best block.
    *
    */
  fun putBlock(db : KeyValueDatabase, blockHash : Hash, block:Block) : Boolean {

    // TODO : BUGBUG : Need to think about RocksDB transactions.

    synchronized(this) {
      if (storage.hasBlock(db, blockHash)) {
        logger.trace("Duplicate block was ignored. Block hash : ${blockHash}")
        return false
      } else {

        // Case 1. If it is the genesis block, set the genesis block as the current best block.
        if (block.header.hashPrevBlock.isAllZero()) {
          assert(theBestBlock == null)

          storage.putBlock(db, block)

          val blockInfo = storage.getBlockInfo(db, blockHash)!!

          // Attach the block. ChainEventListener is invoked in this method.
          // TODO : BUGBUG : Before attaching a block, we need to test if all transactions in the block can be attached.
          // - If any of them are not attachable, the blockchain remains in an inconsistent state because only part of transactions are attached.
          blockMagnet.attachBlock(db, blockInfo, block)

          setBestBlock(db, blockHash, blockInfo )

          return true
        } else { // Case 2. Not a genesis block.
          assert(theBestBlock != null)

          // Step 2.1 : Get the block descriptor of the previous block.
          // We already checked if the parent block exists so it is safe to call with '!!'
          val prevBlockDesc: BlockInfo = storage.getBlockInfo(db, block.header.hashPrevBlock)!!

          val prevBlockHash = prevBlockDesc.blockHeader.hash()

          storage.putBlock(db, block)
          val blockInfo = storage.getBlockInfo(db, blockHash)!!

          // Case 2.A : The previous block of the block is the current best block.
          if (prevBlockHash == theBestBlock!!.blockHeader.hash() ) {
            // Step 2.A.1 : Attach the block. ChainEventListener is invoked in this method.
            blockMagnet.attachBlock(db, blockInfo, block)

            // Step 2.A.2 : Update the best block
            setBestBlock(db, blockHash, blockInfo )

            // TODO : Update best block in wallet (so we can detect restored wallets)
            logger.info("Successfully have put the block in the best blockchain.\n Height : ${blockInfo.height}, Hash : ${blockHash}")
            return true
          } else { // Case 2.B : The previous block of the new block is NOT the current best block.
            // Step 3.B.1 : See if the chain work of the new block is greater than the best one.
            if (blockInfo.chainWork > theBestBlock!!.chainWork) {
              logger.info("Block reorganization started. Original Best : (${theBestBlock!!.blockHeader.hash()},${theBestBlock}), The Best (${blockInfo.blockHeader.hash()},${blockInfo})")

              // Step 3.B.2 : Reorganize the blocks.
              // transaction handling, orphan block handling is done in this method.
              blockMagnet.reorganize(db, originalBestBlock = theBestBlock!!, newBestBlock = blockInfo)


              // Step 3.B.3 : Update the best block
              setBestBlock(db, blockHash, blockInfo)

              // TODO : Update best block in wallet (so we can detect restored wallets)
              return true
            } else {
              logger.info("A block was added to a fork. The current Best : (${theBestBlock!!.blockHeader.hash()},${theBestBlock}), The best on the fork : (${blockInfo.blockHeader.hash()},${blockInfo})")
              return false
            }
          }
        }
      }
    }
  }

  /*
    /** Put transactions into the transaction index.
      * Key : transaction hash
      * Value : FileRecordLocator for the transaction.
      *
      * @param transactions The list of transactions to put.
      */
    fun putTransactions(transactions : List<(Transaction, TransactionLocator)>) : Unit {
      for ( (transaction, txLocatorDesc) <- transactions) {

        // We may already have a transaction descriptor for the transaction.
        val txDescOption = storage.getTransactionDescriptor(txLocatorDesc.txHash)
        // Keep the outputs spent by if it already exists.
        val outpusSpentBy = txDescOption.map( _.outputsSpentBy).getOrElse( List.fill(transaction.outputs.length)(None) )
        val txDesc =
          TransactionDescriptor(
            Some(txLocatorDesc.txLocator),
            outpusSpentBy
          )

        storage.putTransactionDescriptor(txLocatorDesc.txHash, txDesc)

        assert( txLocatorDesc.txHash == transaction.hash )
      }
    }
  */

  /*
    /**
      * Put a block header. The logic is almost identical to the putBlock method except the block reorganization part.
      *
      * Note : the next block hash is not updated.
      *
      * @param blockHash The hash of the block header.
      * @param blockHeader The block header.
      */
    fun putBlockHeader(blockHash : Hash, blockHeader:BlockHeader) : Unit {
      // TODO : Implement
      logger.warn("Headers-first IBD is not supported yet.")
      assert(false)
    }
  */
  /** Put a transaction we received from peers into the disk-pool.
    *
    * @param transaction The transaction to put into the disk-pool.
    */
  fun putTransaction(db : KeyValueDatabase, txHash : Hash, transaction : Transaction) : Unit {
    // TODO : BUGBUG : Need to start a RocksDB transaction.
    try {
      // Step 1 : Add transaction to the transaction pool.
      txPool.addTransactionToPool(db, txHash, transaction)

      // TODO : BUGBUG : Need to commit the RocksDB transaction.

    } finally {
      // TODO : BUGBUG : Need to rollback the RocksDB transaction if any exception raised.
      // Only some of inputs might be connected. We need to revert the connection if any error happens.
    }
  }

  /** Return an iterator that iterates each ChainBlock from a given height.
    *
    * Used by : importAddress RPC to rescan the blockchain.
    *
    * @param height Specifies where we start the iteration. The height 0 means the genesis block.
    * @return The iterator that iterates each ChainBlock.
    */
  override fun getIterator(db : KeyValueDatabase, height : Long) : Iterator<ChainBlock> {
    // TODO : Implement
    throw UnsupportedOperationException()
  }

  /** Return the block height of the best block.
    *
    * @return The best block height.
    */
  override fun getBestBlockHeight() : Long {
    assert(theBestBlock != null)
    return theBestBlock!!.height
  }

  /** Return the hash of block on the tip of the best blockchain.
    *
    * @return The best block hash.
    */
  fun getBestBlockHash(db : KeyValueDatabase) : Hash? {
    return storage.getBestBlockHash(db)
  }

  /** Get the hash of a block specified by the block height on the best blockchain.
    *
    * Used by : getblockhash RPC.
    *
    * @param blockHeight The height of the block.
    * @return The hash of the block header.
    */
  fun getBlockHash(db : KeyValueDatabase, blockHeight : Long) : Hash {
    val blockHashOption = storage.getBlockHashByHeight(db, blockHeight)
    // TODO : Bitcoin Compatiblity : Make the error code compatible when the block height was a wrong value.
    if (blockHashOption == null) {
      throw ChainException(ErrorCode.InvalidBlockHeight)
    }
    return blockHashOption
  }

  /**
    * Get the block info of a block searching by a block hash.
    *
    * Used by BlockLocator to get the info of the given block.
    *
    * @param blockHash The hash of the block to get the info of it.
    * @return Some(blockInfo) if the block exists; None otherwise.
    */
  fun getBlockInfo(db : KeyValueDatabase, blockHash : Hash) : BlockInfo? {
    return storage.getBlockInfo(db, blockHash)
  }



  /** See if a block exists on the blockchain.
    *
    * Used by : submitblock RPC to check if a block already exists.
    *
    * @param blockHash The hash of the block header to check.
    * @return true if the block exists; false otherwise.
    */
  fun hasBlock(db : KeyValueDatabase, blockHash : Hash) : Boolean {
    return storage.hasBlock(db, blockHash)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  fun getBlock(db : KeyValueDatabase, blockHash : Hash) : Pair<BlockInfo, Block>? {
    return storage.getBlock(db, blockHash)
  }


  /** Get a block header by the header hash.
    *
    * @param blockHash The hash of the block header.
    * @return The block header.
    */
  fun getBlockHeader(db : KeyValueDatabase, blockHash : Hash) : BlockHeader? {
    return storage.getBlockHeader(db, blockHash)
  }

  /** Return a transaction that matches the given transaction hash.
    *
    * Used by listtransaction RPC to get the
    *
    * @param txHash The transaction hash to search.
    * @return Some(transaction) if the transaction that matches the hash was found. None otherwise.
    */
  override fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction? {
    // Note : No need to search transaction pool, as storage.getTransaction searches the transaction pool as well.

    // Step 1 : Search block database.
    val dbTransactionOption = storage.getTransaction(db, transactionHash)

    // Step 3 : TODO : Run validation.

    //BUGBUG : Transaction validation fails because the transaction hash on the outpoint does not exist.
    //poolTransactionOption.foreach( TransactionVerifier(_).verify(DiskBlockStorage.get) )
    //dbTransactionOption.foreach( TransactionVerifier(_).verify(DiskBlockStorage.get) )

    return dbTransactionOption
  }

  /**
    * Get the block info of the block which has the given transaction.
    *
    * @param txHash The hash of the transaction to get the block info of the block which has the transaction.
    * @return Some(block info) if the transaction is included in a block; None otherwise.
    */
  fun getTransactionBlockInfo(db : KeyValueDatabase, txHash : Hash ) : BlockInfo? {
    val txDescOption = storage.getTransactionDescriptor(db, txHash)
    if (txDescOption != null) {
      val blockHash = getBlockHash(db, txDescOption.blockHeight)
      return getBlockInfo(db, blockHash)!!
    } else {
      return null
    }
  }

  /** Check if the transaction exists either in a block on the best blockchain or on the transaction pool.
    *
    * @param txHash The hash of the transaction to check the existence.
    * @return true if we have the transaction; false otherwise.
    */
  fun hasTransaction(db : KeyValueDatabase, txHash : Hash) : Boolean {
    return storage.getTransactionDescriptor(db, txHash) != null || storage.getTransactionFromPool(db, txHash) != null
  }

  /** Return a transaction output specified by a give out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  override fun getTransactionOutput(db : KeyValueDatabase, outPoint : OutPoint) : TransactionOutput {
    // Coinbase outpoints should never come here
    assert(!outPoint.transactionHash.isAllZero())

    val transaction = getTransaction(db, outPoint.transactionHash)
    if (transaction == null) {
      val message = "The transaction pointed by an outpoint was not found : " + outPoint.transactionHash
      logger.error(message)
      throw ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    val outputs = transaction.outputs

    if (outPoint.outputIndex < 0 || outPoint.outputIndex >= outputs.size) {
      val message = "Invalid output index. Transaction hash : ${outPoint.transactionHash}, Output count : ${outputs.size}, Output index : ${outPoint.outputIndex}, transaction : ${transaction}"
      logger.error(message)
      throw ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    return outputs[outPoint.outputIndex]
  }

  companion object {
    var theBlockchain : Blockchain? = null
    fun create(db : KeyValueDatabase, storage : BlockStorage) : Blockchain {
      theBlockchain = Blockchain(db, storage)

      // Load any in memory structur required by the Blockchain class from the on-disk storage.
      BlockchainLoader(db, theBlockchain!!, storage).load()
      return theBlockchain!!
    }
    fun get() : Blockchain {
      return theBlockchain!!
    }
  }
}

