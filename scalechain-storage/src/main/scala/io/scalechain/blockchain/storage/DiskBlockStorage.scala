package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.db.RocksDatabase
import org.slf4j.LoggerFactory

import scala.collection.mutable

class DiskBlockStorage(directoryPath : File) {
  val logger = LoggerFactory.getLogger(classOf[DiskBlockStorage])


  val blockIndex = new BlockDatabase( new RocksDatabase( directoryPath.getPath ) )
  val blockRecordStorage = new BlockRecordStorage(directoryPath)

  var bestBlockHeightOption = blockIndex.getBestBlockHash().map(blockIndex.getBlockHeight(_).get)
  def checkBestBlockHash(blockHash : Hash, height : Int): Unit = {
    if (bestBlockHeightOption.isEmpty || bestBlockHeightOption.get < height) {
      bestBlockHeightOption = Some(height)
      blockIndex.putBestBlockHash(blockHash)
    }
  }

  def putBlock(block : Block) : Unit = {
    val blockHash = Hash( HashCalculator.blockHeaderHash(block.header) )
    putBlock(blockHash, block)
  }

  /** Store a block.
    *
    * @param blockHash the hash of the header of the block to store.
    * @param block the block to store.
    */
  def putBlock(blockHash : Hash, block : Block) : Unit = {

    val blockInfo : Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)

    val blockLocatorOption =
    if (blockInfo.isDefined) {
      if (blockInfo.get.blockLocatorOption.isEmpty) {
        val blockLocator = blockRecordStorage.appendRecord(block)
        val newBlockInfo= blockInfo.get.copy(blockLocatorOption = Some(blockLocator))
        Some(blockIndex.putBlockInfo(blockHash, newBlockInfo))
      } else {
        // The block already exists. Do not put it once more.
        logger.warn("The block already exists. block hash : {}", blockHash)
        blockInfo.get.blockLocatorOption
      }
    } else {
      // get the height of the previous block, to calculate the height of the given block.
      val prevBlockHeightOption : Option[Int] = blockIndex.getBlockHeight(blockHash)

      if (prevBlockHeightOption.isDefined) {
        val blockLocator = blockRecordStorage.appendRecord(block)

        val blockHeight = prevBlockHeightOption.get + 1
        val blockInfo = BlockInfo(
          height = blockHeight,
          transactionCount = 0,
          // BUGBUG : Need to use enumeration
          status = 0,
          blockHeader = block.header,
          Some(blockLocator)
        )
        blockIndex.putBlockInfo(blockHash, blockInfo)
        checkBestBlockHash(blockHash, blockHeight)
        Some(blockLocator)
      } else {
        logger.warn("An orphan block was discarded while saving a block. block hash : {}", blockHash)
        None
      }
    }

    if ( blockLocatorOption.isDefined) {
      // TODO : Need to construct an array of (tranasction hash, transaction locator) pairs
      //blockIndex.putTransactions(blockLocatorOption.get, block.transactions)
    }
  }

  def putBlockHeader(blockHeader : BlockHeader) : Unit = {
    val blockHash = Hash( HashCalculator.blockHeaderHash(blockHeader) )
    putBlockHeader(blockHash, blockHeader)
  }

  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader) : Unit = {
    // get the height of the previous block, to calculate the height of the given block.
    val prevBlockHeightOption : Option[Int] = blockIndex.getBlockHeight(blockHash)

    if (prevBlockHeightOption.isDefined) {
      val blockHeight = prevBlockHeightOption.get + 1

      assert(blockHeight > 0)

      val blockInfo : Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)
      if (blockInfo.isEmpty) {
        val blockInfo = BlockInfo(
          height = blockHeight,
          transactionCount = 0,
          // BUGBUG : Need to use enumeration
          status = 0,
          blockHeader = blockHeader,
          None
        )
        blockIndex.putBlockInfo(blockHash, blockInfo)
        checkBestBlockHash(blockHash, blockHeight)
      } else {
        // blockIndex hits an assertion if the block header is changed for the same block hash.
        blockIndex.putBlockInfo(blockHash, blockInfo.get.copy(
          blockHeader = blockHeader
        ))
      }
    } else {
      logger.warn("An orphan block was discarded while saving a block header. block hash : {}", blockHash)
    }
  }

  def getTransaction(transactionHash : Hash) : Option[Transaction] = {
    val txLocatorOption = blockIndex.getTransactionLocator(transactionHash)
    //txLocatorOption.map( blockRecordStorage. )
    // TODO : Implement
    assert(false)
    None
  }

  /** Get a block by its hash.
    *
    * @param blockHash The hash of the block header to search.
    */
  def getBlock(blockHash : Hash) : Option[Block] = {
    val blockInfoOption = blockIndex.getBlockInfo(blockHash)
    if (blockInfoOption.isDefined) {
      if (blockInfoOption.get.blockLocatorOption.isDefined) {
        Some( blockRecordStorage.readRecord(blockInfoOption.get.blockLocatorOption.get) )
      } else {
        None
      }
    } else {
      None
    }
  }

  def hasBlock(blockHash : Hash) : Boolean = {
    getBlock(blockHash).isDefined
  }

  def getBlockHeader(blockHash : Hash) : Option[BlockHeader] = {
    val blockInfoOption = blockIndex.getBlockInfo(blockHash)
    if (blockInfoOption.isDefined) {
      Some(blockInfoOption.get.blockHeader)
    } else {
      None
    }
  }

  def hasBlockHeader(blockHash : Hash) : Boolean = {
    getBlockHeader(blockHash).isDefined
  }
}
