package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{BlockHeader, OrphanBlockDescriptor, Block, Hash}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.BlockStorage
import HashSupported._

import scala.annotation.tailrec

/**
  * Created by kangmo on 6/9/16.
  */
class BlockOrphanage(storage : BlockStorage) {
  /** Check if we have the given block as an orphan.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exists as an orphan; false otherwise.
    */
  def hasOrphan(blockHash : Hash) : Boolean = {
    storage.getOrphanBlock(blockHash).isDefined
  }


  /** Put the block as an orphan block.
    *
    * @param block the block to put as an orphan block.
    */
  def putOrphan(block : Block) : Unit = {
    // TODO : BUGBUG : Need to check the max size of RocksDB value, as a block can grow to a big size such as 100MB ?
    // TOOD : BUGBUG : Need to check DoS attack consuming disk storage by sending orphan blocks.
    // TODO : Optimize : Use headers-first approach, or drop orphan blocks.
    // TODO : Need to have maximum number of orphan blocks?
    val blockHash = block.header.hash
    storage.putOrphanBlock(blockHash, OrphanBlockDescriptor(block))
    storage.addOrphanBlockByParent(block.header.hashPrevBlock, blockHash)
  }


  /**
    * Get an orphan block.
    * @param blockHash The hash of the orphan block.
    * @return Some(block) if the orphan was found; None otherwise.
    */
  def getOrphan(blockHash : Hash) : Option[Block] = {
    storage.getOrphanBlock(blockHash).map(_.block)
  }

  /**
    * Remove the block from the orphan blocks.
    *
    * @param block The block to delete from orphans.
    */
  protected[chain] def delOrphan(block : Block) : Unit = {
    storage.delOrphanBlock(block.header.hash)
  }


  @tailrec
  final protected[chain] def getOrphanRoot(blockHeader : BlockHeader) : Hash = {
    val blockHash = blockHeader.hash
    assert(blockHash != blockHeader.blockHeader.hashPrevBlock)
    val parentOrphanOption = storage.getOrphanBlock(blockHeader.blockHeader.hashPrevBlock)
    if (parentOrphanOption.isEmpty) { // The base case. The parent does not exist in the orphanage.
      blockHash
    } else { // We still have the parent in the orphanage.
      getOrphanRoot( parentOrphanOption.get.block.header )
    }
  }

  /**
    * Get the root orphan that does not have its parent even in the orphan blocks.
    * Ex> When B1's parent is B0, B2's parent is B1 and B0 is missing, the orphan root of the B2 is B1.
    *
    * @param blockHash The block to find the root parent of it.
    * @return The hash of the orphan block whose parent is missing even in the orphan blocks list.
    */
  def getOrphanRoot(blockHash : Hash) : Hash = {
    val orphanOption = storage.getOrphanBlock(blockHash)
    // getOrphanRoot should never called to a non-orphan block.
    // TODO : Make sure that concurrent threads can't delete the orphan block while getOrphanRoot is called.
    assert( orphanOption.isDefined )

    getOrphanRoot(orphanOption.get.block.header)
  }

  /** Get the list of orphan block hashes depending a given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  def getOrphansDependingOn(blockHash : Hash) : List[Hash] = {
    storage.getOrphanBlocksByParent(blockHash)
  }


  /** Remove dependencies on an orphan parent.
    *
    * @param blockHash The parent block hash.
    */
  def removeDependenciesOn(blockHash : Hash) : Unit = {
    storage.delOrphanBlocksByParent(blockHash)
  }
}
