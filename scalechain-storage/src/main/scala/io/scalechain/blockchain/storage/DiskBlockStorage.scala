package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.db.RocksDatabase

import scala.collection.mutable

class DiskBlockStorage(directoryPath : File) {

  val blockIndex = new BlockDatabase( new RocksDatabase( directoryPath.getPath ) )
  val blockRecordStorage = new BlockRecordStorage(directoryPath)

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
    val blockLocator = blockRecordStorage.appendRecord(block)

    val blockInfo : Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)

    if (blockInfo.isDefined) {
      assert(blockInfo.get.blockLocatorOption.isEmpty)
      val newBlockInfo= blockInfo.get.copy(blockLocatorOption = Some(blockLocator))
      blockIndex.putBlockInfo(blockHash, newBlockInfo)
      blockIndex.putTransactions(blockLocator, block.transactions)
    } else {
      assert(false);
    }
  }

  def putBlockHeader(blockHeader : BlockHeader) : Unit = {
    // TODO : Get the block height of the given block.
    assert(false);
    val blockHeight = -1

    val blockHash = Hash( HashCalculator.blockHeaderHash(blockHeader) )

    putBlockHeader(blockHash, blockHeader, blockHeight)
  }

  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader, height : Int ) : Unit = {
    assert(height >= 0)

    val blockInfo : Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)
    if (blockInfo.isEmpty) {
      val blockInfo = BlockInfo(
        height = height,
        transactionCount = 0,
        // BUGBUG : Need to use enumeration
        status = 0,
        blockHeader = blockHeader,
        None
      )
      blockIndex.putBlockInfo(blockHash, blockInfo)
    } else {
      assert(false)
    }
  }

  def getTransaction(transactionHash : Hash) : Option[Transaction] = {
    assert(false)
    null
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

  def hasBlockHash(blockHash : Hash) : Boolean = {
    getBlockHeader(blockHash).isDefined
  }
}
