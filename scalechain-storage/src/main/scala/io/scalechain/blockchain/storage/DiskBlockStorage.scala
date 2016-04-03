package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.index.{BlockDatabaseForRecordStorage, BlockDatabase, RocksDatabase}
import io.scalechain.blockchain.storage.record.BlockRecordStorage
import org.slf4j.LoggerFactory


// A version Using DiskBlockStorage
object DiskBlockStorage {
  val MAX_FILE_SIZE = 1024 * 1024 * 100
  //val MAX_FILE_SIZE = 1024 * 1024 * 1


  var theBlockStorage : DiskBlockStorage = null

  def create(storagePath : File) : DiskBlockStorage = {
    assert(theBlockStorage == null)
    theBlockStorage = new DiskBlockStorage(storagePath, MAX_FILE_SIZE)

    // See if we have genesis block. If not, put one.
    if ( ! theBlockStorage.hasBlock(GenesisBlock.HASH) ) {
      theBlockStorage.putBlock(GenesisBlock.BLOCK)
    }

    theBlockStorage
  }

  /** Get the block storage. This actor is a singleton, used by transaction validator.
    *
    * @return The block storage.
    */
  def get() : DiskBlockStorage = {
    assert(theBlockStorage != null)
    theBlockStorage
  }

}

/*
// A version Using CassandraBlockStorage
object DiskBlockStorage {
  val MAX_FILE_SIZE = 1024 * 1024 * 100
  //val MAX_FILE_SIZE = 1024 * 1024 * 1


  var theBlockStorage : CassandraBlockStorage = null

  def create(storagePath : File) : CassandraBlockStorage = {
    assert(theBlockStorage == null)
    theBlockStorage = new CassandraBlockStorage(storagePath)

    // See if we have genesis block. If not, put one.
    if ( ! theBlockStorage.hasBlock(GenesisBlock.HASH) ) {
      theBlockStorage.putBlock(GenesisBlock.BLOCK)
    }

    theBlockStorage
  }

  /** Get the block storage. This actor is a singleton, used by transaction validator.
    *
    * @return The block storage.
    */
  def get() : CassandraBlockStorage = {
    assert(theBlockStorage != null)
    theBlockStorage
  }

}
*/

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

class DiskBlockStorage(directoryPath : File, maxFileSize : Int) extends BlockStorage {
  private val logger = LoggerFactory.getLogger(classOf[DiskBlockStorage])

  directoryPath.mkdir()

  protected[storage] val blockIndex = new BlockDatabaseForRecordStorage( new RocksDatabase( directoryPath ) )
  protected[storage] val blockRecordStorage = new BlockRecordStorage(directoryPath, maxFileSize)
  protected[storage] val blockWriter = new BlockWriter(blockRecordStorage)

  protected[storage] def blockDatabase() : BlockDatabase = {
    blockIndex
  }

  protected[storage] def updateFileInfo(headerLocator : FileRecordLocator, fileSize : Long, blockHeight : Int, blockTimestamp : Long): Unit = {
    val lastFileNumber = FileNumber(headerLocator.fileIndex)

    // Are we writing at the beginning of the file?
    // If yes, we need to update the last block file, because it means we created a file now.

    if (headerLocator.recordLocator.offset == 0) { // case 1 : a new record file was created.
      blockIndex.putLastBlockFile(lastFileNumber)
    } else { // case 2 : the block was written on the existing record file.
      // do nothing.
    }

    // Update the block info.
    val blockFileInfo = blockIndex.getBlockFileInfo(lastFileNumber).getOrElse(
      BlockFileInfo(
        blockCount = 0,
        fileSize = 0L,
        firstBlockHeight = blockHeight,
        lastBlockHeight = blockHeight,
        firstBlockTimestamp = blockTimestamp,
        lastBlockTimestamp = blockTimestamp
      )
    )

    // TODO : Need to make sure if it is ok even though a non-best block decreases the lastBlockHeight.
    // Is the lastBlockHeight actually meaning maximum block height?
    blockIndex.putBlockFileInfo(
      lastFileNumber,
      blockFileInfo.copy(
        blockCount = blockFileInfo.blockCount + 1,
        fileSize = fileSize,
        lastBlockHeight = blockHeight,
        lastBlockTimestamp = blockTimestamp
      )
    )
  }

  /** Store a block.
    *
    * @param blockHash the hash of the header of the block to store.
    * @param block the block to store.
    *
    * @return Boolean true if the block header or block was not existing, and it was put for the first time. false otherwise.
    *                 submitblock rpc uses this method to check if the block to submit is a new one on the database.
    */
  def putBlock(blockHash : Hash, block : Block) : Boolean = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {

      val blockInfo: Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)
      var isNewBlock = false

      val txLocators: List[TransactionLocator] =
        if (blockInfo.isDefined) {
          // case 1 : block info was found
          if (blockInfo.get.blockLocatorOption.isEmpty) {
            // case 1.1 : block info without a block locator was found
            val appendResult = blockWriter.appendBlock(block)
            val newBlockInfo = blockInfo.get.copy(
              transactionCount = block.transactions.size,
              blockLocatorOption = Some(appendResult.blockLocator)
            )
            blockIndex.putBlockInfo(blockHash, newBlockInfo)
            val fileSize = blockRecordStorage.files(appendResult.headerLocator.fileIndex).size
            updateFileInfo(appendResult.headerLocator, fileSize, newBlockInfo.height, block.header.timestamp)
            checkBestBlockHash(blockHash, newBlockInfo.height)

            //logger.info("The block locator was updated. block hash : {}", blockHash)
            appendResult.txLocators
          } else {
            // case 1.2 block info with a block locator was found
            // The block already exists. Do not put it once more.
            logger.warn("The block already exists. block hash : {}", blockHash)

            List()
          }
        } else {
          // case 2 : no block info was found.
          // get the height of the previous block, to calculate the height of the given block.
          val prevBlockHeightOption: Option[Int] = getBlockHeight(Hash(block.header.hashPrevBlock.value))

          if (prevBlockHeightOption.isDefined) {
            // case 2.1 : no block info was found, previous block header exists.
            val appendResult = blockWriter.appendBlock(block)

            val blockHeight = prevBlockHeightOption.get + 1
            val blockInfo = BlockInfo(
              height = blockHeight,
              transactionCount = block.transactions.size,
              // BUGBUG : Need to use enumeration
              status = 0,
              blockHeader = block.header,
              Some(appendResult.blockLocator)
            )
            blockIndex.putBlockInfo(blockHash, blockInfo)

            val fileSize = blockRecordStorage.files(appendResult.headerLocator.fileIndex).size
            updateFileInfo(appendResult.headerLocator, fileSize, blockInfo.height, block.header.timestamp)
            checkBestBlockHash(blockHash, blockHeight)

            isNewBlock = true
            //logger.info("The new block was put. block hash : {}", blockHash)

            appendResult.txLocators
          } else {
            // case 2.2 : no block info was found, previous block header does not exists.
            logger.warn("An orphan block was discarded while saving a block. block hash : {}", block.header)

            List()
          }
        }

      if (!txLocators.isEmpty) {
        // case 1.1 and case 2.1 has newly stored transactions.
        blockIndex.putTransactions(txLocators)
      }

      isNewBlock
    }
  }

  def getTransaction(transactionHash : Hash) : Option[Transaction] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      val txLocatorOption = blockIndex.getTransactionLocator(transactionHash)
      txLocatorOption.map(blockRecordStorage.readRecord(_)(TransactionCodec))
    }
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      val blockInfoOption = blockIndex.getBlockInfo(blockHash)
      if (blockInfoOption.isDefined) {
        // case 1 : The block info was found.
        if (blockInfoOption.get.blockLocatorOption.isDefined) {
          //logger.info(s"getBlock - Found a block info with a locator. block hash : ${blockHash}, locator : ${blockInfoOption.get.blockLocatorOption}")
          // case 1.1 : the block info with a block locator was found.
          Some( (blockInfoOption.get, blockRecordStorage.readRecord(blockInfoOption.get.blockLocatorOption.get)(BlockCodec)) )
        } else {
          // case 1.2 : the block info without a block locator was found.
          //logger.info("getBlock - Found a block info without a locator. block hash : {}", blockHash)
          None
        }
      } else {
        // case 2 : The block info was not found
        //logger.info("getBlock - No block info found. block hash : {}", blockHash)
        None
      }
    }
  }

  def close() : Unit = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      blockRecordStorage.close()
      blockIndex.close()
    }
  }
}
