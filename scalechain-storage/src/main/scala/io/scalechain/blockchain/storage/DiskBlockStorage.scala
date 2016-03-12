package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{TransactionCodec, BlockCodec}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.index.{BlockDatabase, RocksDatabase}
import io.scalechain.blockchain.storage.record.BlockRecordStorage
import org.slf4j.LoggerFactory

import scala.collection.mutable

class DiskBlockStorage(directoryPath : File) extends BlockIndex {
  private val logger = LoggerFactory.getLogger(classOf[DiskBlockStorage])

  protected[storage] val blockIndex = new BlockDatabase( new RocksDatabase( directoryPath.getPath ) )
  protected[storage] val blockRecordStorage = new BlockRecordStorage(directoryPath)

  protected[storage] var bestBlockHeightOption = blockIndex.getBestBlockHash().map(blockIndex.getBlockHeight(_).get)

  protected[storage] def checkBestBlockHash(blockHash : Hash, height : Int): Unit = {
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
        val blockLocator = blockRecordStorage.appendRecord(block)(BlockCodec)
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
        val blockLocator = blockRecordStorage.appendRecord(block)(BlockCodec)

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
    txLocatorOption.map( blockRecordStorage.readRecord(_)(TransactionCodec) )
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : Hash) : Option[Block] = {
    val blockInfoOption = blockIndex.getBlockInfo(blockHash)
    if (blockInfoOption.isDefined) {
      if (blockInfoOption.get.blockLocatorOption.isDefined) {
        Some( blockRecordStorage.readRecord(blockInfoOption.get.blockLocatorOption.get)(BlockCodec) )
      } else {
        None
      }
    } else {
      None
    }
  }


  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  def getBestBlockHash() : Option[Hash] = {
    blockIndex.getBestBlockHash()
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

  def getBlock(blockHash : BlockHash) : Option[Block] = {
    getBlock(Hash(blockHash.value))
  }

  def getTransaction(transactionHash : TransactionHash) : Option[Transaction] = {
    getTransaction(Hash(transactionHash.value))
  }
}
