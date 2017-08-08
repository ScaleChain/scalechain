package io.scalechain.blockchain.storage



import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.storage.index.*
import io.scalechain.blockchain.script.*
import org.slf4j.LoggerFactory


/** Stores block header, block, and transactions in the block.
 *
 * Blocks are stored in two cases.
 *
 * 1. During IBD(Initial block download) process
 *   * We use headers-first approach, so we download all headers from other peers first.
 *     These headers are kept in the key/value database first.
 *   * After all headers are downloaded, we download block data from other peers.
 *     When we store blocks, we store them on the record storage, which writes records on blkNNNNN.dat file.
 *   * After the block data is stored, we update the block info on the key/value database
 *     to point to the record locator on the record storage.
 *
 * 2. After IBD process, we receive a block per (about) 10 minutes
 *    In this case, both header and block data comes together.
 *    We put both block and block header at once.
 *
 *
 * Upon receival of blocks, we maintain the following indexes.
 * keys and values are stored on the key/value database, whereas records are stored on the record storage.
 *
 * 1. (key) block hash -> (value) (block info) record locator -> (record) block data
 * 2. (key) transaction hash -> (value) record locator -> (record) a transaction in the block data.
 * 3. (key) file number -> (value) block file info
 * 4. (key) static -> (value) best block hash
 * 5. (key) static -> (value) last block file number
 *
 * How block headers and blocks are stored :
 *
 * 1. Only blockheader is stored -> A block data is stored. (OK)
 * 2. A block is stored with block header at once. (OK)
 * 3. A block is stored twice. => The second block data is ignored. A warning message is logged.
 * 4. A blockheader is stored twice. => The second header data is ignored. A warning message is logged.
 *
 * @param directoryPath The path where database files are located.
 */

class SpannerBlockStorage(private val db : KeyValueDatabase, private val instanceId : String, private val databaseId : String, private val transactionTableName : String, private val blockTableName : String) : BlockStorage, BlockDatabaseForRecordStorage {
  private val logger = LoggerFactory.getLogger(DiskBlockStorage::class.java)

  init {
  }

  val transactionTable = SpannerDatabase(instanceId, databaseId, transactionTableName)
  val blockTable = SpannerDatabase(instanceId, databaseId, blockTableName)

  /** Store a block.
   *
   * @param blockHash the hash of the header of the block to store.
   * @param block the block to store.
   * @return Boolean true if the block header or block was not existing, and it was put for the first time. false otherwise.
   *                 submitblock rpc uses this method to check if the block to submit is a new one on the database.
   */
  override fun putBlock(db : KeyValueDatabase, blockHash : Hash, block : Block) : Unit {
    val blockInfo : BlockInfo? = getBlockInfo(db, blockHash)

    val dummyLocator = FileRecordLocator(0, RecordLocator(0,0))

    if (blockInfo != null) {
      // case 1 : block info was found
      if (blockTable.get(blockHash.value.array) == null) {
        // case 1.1 : The block was not found on the blocks table.
        putBlockToSpanner(blockHash, block)

        // block locator - Need to put a dummy so that BlockStorage.hasBlock returns true.
        putBlockInfo(db, blockHash, blockInfo.copy(
          blockLocatorOption = dummyLocator
        ))

        //logger.info("The block data was updated. block hash : {}", blockHash)
      } else {
        // case 1.2 block info with a block locator was found
        // The block already exists. Do not put it once more.
        // This can happen when the mining node already had put the block, but other nodes tried to put it once more.
        // (Because a cassandra cluster is shared by all nodes )
        logger.warn("The block already exists. block hash : {}", blockHash)
      }
    } else {
      // case 2 : no block info was found.
      // get the info of the previous block, to calculate the height and chain-work of the given block.
      val prevBlockInfoOption: BlockInfo? = getBlockInfo(db, Hash(block.header.hashPrevBlock.value))

      // Either the previous block should exist or the block should be the genesis block.
      if (prevBlockInfoOption != null || block.header.hashPrevBlock.isAllZero()) {
        // case 2.1 : no block info was found, previous block header exists.

        val blockInfo = BlockInfoFactory.create(
          prevBlockInfoOption,
          block.header,
          blockHash,
          block.transactions.size, // transaction count
          dummyLocator // block locator - Need to put a dummy so that BlockStorage.hasBlock returns true.
        )

        putBlockInfo(db, blockHash, blockInfo)

        putBlockToSpanner(blockHash, block)

        //logger.info("The new block was put. block hash : {}", blockHash)
      } else {
        // case 2.2 : no block info was found, previous block header does not exists.
        // Actually the code execution should never come to here, because we have checked if the block is an orphan block
        // before invoking putBlock method.
        logger.trace("An orphan block was discarded while saving a block. block hash : {}", block.header)
      }
    }
  }

  protected fun putBlockToSpanner(blockHash : Hash, block : Block) : Unit  {
    for (transaction in block.transactions) {
      // case 1.1 and case 2.1 has newly stored transactions.
      transactionTable.put(
        transaction.hash().value.array,
        TransactionCodec.encode(transaction))
    }

    blockTable.put( blockHash.value.array, BlockCodec.encode(block))
  }


  /** Return a transaction that matches the given transaction hash.
   *
   * TODO : Add test case.
   *
   * @param transactionHash
   * @return
   */
  override fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction? {
    val serializedTransactionOption = transactionTable.get(transactionHash.value.array)
    if (serializedTransactionOption != null) {
      return TransactionCodec.decode(serializedTransactionOption)
    } else {
      return getTransactionFromPool(db, transactionHash)?.transaction
    }
  }

  /** Get a block searching by the header hash.
   *
   * Used by : getblock RPC.
   *
   * @param blockHash The header hash of the block to search.
   * @return The searched block.
   */
  override fun getBlock(db : KeyValueDatabase, blockHash : Hash) : Pair<BlockInfo, Block>? {
    val blockInfoOption = getBlockInfo(db, blockHash)
    if (blockInfoOption != null) {
      // case 1 : The block info was found.
      val serializedBlockOption = blockTable.get(blockHash.value.array)
      if (serializedBlockOption != null) {
        return Pair(blockInfoOption, BlockCodec.decode(serializedBlockOption)!! )
      } else {
        return null
      }
    } else {
      // case 2 : The block info was not found
      //logger.info("getBlock - No block info found. block hash : {}", blockHash)
      return null
    }
  }

  override fun close() : Unit {
    blockTable.close()
    transactionTable.close()
  }

  companion object {
    lateinit var theBlockStorage : SpannerBlockStorage

    fun create(db : KeyValueDatabase, instanceId : String, databaseId : String) : BlockStorage {
      theBlockStorage = SpannerBlockStorage(db, instanceId, databaseId, "transaction", "block")
      return theBlockStorage
    }

    /** Get the block storage. This actor is a singleton, used by transaction validator.
     *
     * @return The block storage.
     */
    fun get() : BlockStorage {
      return theBlockStorage
    }
  }
}

