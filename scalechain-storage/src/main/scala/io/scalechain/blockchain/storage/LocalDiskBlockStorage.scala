package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.{Block, Hash, BlockHeader}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.RecordStorage.FileRecordLocator

import scala.collection.mutable

case class Locators(headerLocator : FileRecordLocator, blockLocator : FileRecordLocator)

class HashToFileRecordLocator {
  // TODO : Implement put/get using leveldb to persistently store the index.
  val recordLocatorByHash = mutable.HashMap[Hash, Locators]()

  def put(hash : Hash, locators : Locators) = recordLocatorByHash.put(hash, locators)
  def get(hash : Hash) = recordLocatorByHash.get(hash)
}

/** Store blocks onto disk keeping indexes by (1) block hash (2) transaction hash.
  * The raw blocks are stored on a file.
  * The indexes are built using Java implementation of LevelDB.
  * https://github.com/dain/leveldb
  */
class LocalDiskBlockStorage(directoryPath : File) extends BlockStorage {
  val locatorMap = new HashToFileRecordLocator()

  val headerRecordStorage = new HeaderRecordStorage(directoryPath)
  val blockRecordStorage = new BlockRecordStorage(directoryPath)

  def storeHeader(header : BlockHeader) : Unit = {
    // TODO : Optimize : both appendRecord and blockHeaderHash serializes the block header.
    val hash = Hash( HashCalculator.blockHeaderHash(header) )

    val locator = headerRecordStorage.appendRecord(header)

    // BUGBUG : check if the locator already exists. Throw an exception if exists.
    val locators = locatorMap.get(hash)
                      .getOrElse(Locators(null, null))
                      .copy(headerLocator = locator)

    locatorMap.put(hash, locators)
  }

  def getHeader(hash:Hash) : Option[BlockHeader] = {
    locatorMap.get(hash).filter(_.headerLocator != null ).map{ locators =>
      headerRecordStorage.readRecord( locators.headerLocator )
    }
  }

  def hasHeader(hash:Hash) : Boolean = {
    locatorMap.get(hash).exists(_.headerLocator != null )
  }

  def storeBlock(block : Block) : Unit = {
    // TODO : Optimize : both appendRecord and blockHeaderHash serializes the block header.
    val hash = Hash( HashCalculator.blockHeaderHash(block.header) )

    val locator = blockRecordStorage.appendRecord(block)

    // BUGBUG : check if the header locator already exists. Throw an exception if it does not exists.
    val locators = locatorMap.get(hash)
                      .getOrElse(Locators(null, null))
                      .copy(blockLocator = locator)

    locatorMap.put(hash, locators)
  }

  def getBlock(hash : Hash) : Option[Block] = {
    locatorMap.get(hash).filter(_.blockLocator != null ).map{ locators =>
      blockRecordStorage.readRecord( locators.blockLocator )
    }
  }

  def hasBlock(hash:Hash) : Boolean = {
    locatorMap.get(hash).exists(_.blockLocator != null )
  }
}
