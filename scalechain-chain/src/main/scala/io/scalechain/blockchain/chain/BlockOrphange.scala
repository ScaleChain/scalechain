package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{Block, Hash}
import io.scalechain.blockchain.storage.BlockStorage

/**
  * Created by kangmo on 6/9/16.
  */
class BlockOrphange(storage : BlockStorage) {
  /** Check if we have the given block as an orphan.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exists as an orphan; false otherwise.
    */
  def hasOrphan(blockHash : Hash) : Boolean = {
    // TODO : Implement
    assert(false)
    false
  }


  /** Put the block as an orphan block.
    *
    * @param block the block to put as an orphan block.
    */
  def putOrphan(block : Block) : Unit = {
    // TODO : Implement
    assert(false)
  }


  /**
    * Remove the block from the orphan blocks.
    *
    * @param block The block to delete from orphans.
    */
  protected[chain] def delOrphan(block : Block) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /**
    * Get the root parent that is missing for the given block.
    * Ex> When B1's parent is B0, B2's parent is B1 and B0 is missing, the orphan root of the B2 is B0.
    *
    * @param blockHash The block to find the root parent of it.
    * @return The hash of the (missing) root parent.
    */
  def getOrphanRoot(blockHash : Hash) : Hash = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get the list of orphan block hashes depending a given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  def getOrphansDependingOn(blockHash : Hash) : List[Hash] = {
    // TODO : Implement
    assert(false)
    null
  }
}
