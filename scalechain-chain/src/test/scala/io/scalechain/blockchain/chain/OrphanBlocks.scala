package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{Block, BlockHash}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Keeps a list of blocks.
  */
class Blocks {
  /** The list of blocks to keep.
    */
  private val blocks = ListBuffer[Block]()

  /** Put a block on the list of blocks.
    *
    * @param block The block to put.
    */
  def putBlock( block : Block) : Unit = {
    blocks += block
  }

  /** Get the list of blocks we are keeping.
    *
    * @return The list of blocks.
    */
  def getBlockList() = blocks.toList
}

/** Keep a list of orphan blocks for each (missing) parent block.
  * Whenever we receive a block, it is kept in OrphanBlocks if its parent is not received yet.
  */
class OrphanBlocks {
  /** Keep a list of blocks that has the same parent block.
    */
  private val blocksByParentHash = mutable.HashMap[BlockHash, Blocks]()

  /** Put a block as an orphan of a specific parent.
    *
    * @param parentBlockHash The hash of the parent block.
    * @param orphanBlock The orphan block.
    */
  def putBlock(parentBlockHash : BlockHash, orphanBlock : Block) : Unit = {
    // Step 1 : See if we have any orphan block for the same parent block hash.
    val blocks = blocksByParentHash.get(parentBlockHash)

    if (blocks.isDefined) {
      // Step 2.A : put the orphan block into the list of blocks for the parent block hash.
      blocks.get.putBlock(orphanBlock)
    } else {
      // Step 2.A : Create a new Blocks object, add the orphan block.
      val blocks = new Blocks()
      blocks.putBlock(orphanBlock)
      blocksByParentHash.put(parentBlockHash, blocks)
    }
  }

  /** Find orphan blocks by the hash of the parent block.
    *
    * @param parentBlockHash The hash of the parent for looking up orphans.
    * @return Some orphan blocks if any orphan was found. None otherwise.
    */
  def findBlocks(parentBlockHash : BlockHash) : Option[Blocks] = {
    blocksByParentHash.get(parentBlockHash)
  }

  /** Remove all orphan blocks whose parent matches the parent block hash.
    *
    * @param parentBlockHash The hash of the parent.
    */
  def removeBlocks(parentBlockHash : BlockHash) : Unit = {
    blocksByParentHash.remove(parentBlockHash)
  }
}
