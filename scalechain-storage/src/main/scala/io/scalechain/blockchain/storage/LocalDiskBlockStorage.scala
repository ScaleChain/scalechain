package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.{Block, Hash, BlockHeader}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.RecordStorage.FileRecordLocator

import scala.collection.mutable

/** Maintains a map from hash to a record locator.
  *
  * Why?
  *   To get a block by block hash, we need to get the location of the block corresponding to the hash.
  *   As a block is stored on a record-based file, we need a FileRecordLocator to get a specific block(=record) from the file.
  */
class HashToFileRecordLocator {
  // TODO : Implement put/get using leveldb to persistently store the index.
  val recordLocatorByHash = mutable.HashMap[Hash, FileRecordLocator]()


  def put(hash : Hash, locator : FileRecordLocator) = recordLocatorByHash.put(hash, locator)
  def get(hash : Hash) = recordLocatorByHash.get(hash)
}

/** Store blocks onto disk keeping indexes by (1) block hash (2) transaction hash.
  * The raw blocks are stored on a file.
  *
  * The indexes are built using Java implementation of LevelDB.
  * https://github.com/dain/leveldb
  */
class LocalDiskBlockHeaderStorage(directoryPath : File) extends BlockHeaderStorage {
  val locatorMap = new HashToFileRecordLocator()

  val headerRecordStorage = new HeaderRecordStorage(directoryPath)

  def store(hash: Hash, header: BlockHeader): Unit = {
    // TODO : Optimize : both appendRecord and blockHeaderHash serializes the block header.
//    val hash = Hash(HashCalculator.blockHeaderHash(header))

    assert( locatorMap.get(hash).isEmpty )

    val locator = headerRecordStorage.appendRecord(header)

    locatorMap.put(hash, locator)
  }

  def get(hash: Hash): Option[BlockHeader] = {
    locatorMap.get(hash).map { locator =>
      headerRecordStorage.readRecord(locator)
    }
  }

  def exists(hash: Hash): Boolean = {
    locatorMap.get(hash).isDefined
  }
}

class LocalDiskBlockStorage(directoryPath : File) extends BlockStorage {
  val locatorMap = new HashToFileRecordLocator()

  val blockRecordStorage = new BlockRecordStorage(directoryPath)

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
