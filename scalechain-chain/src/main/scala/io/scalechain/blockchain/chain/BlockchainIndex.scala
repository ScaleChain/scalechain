package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Hash

import scala.collection.mutable

/** An index for looking up transactions and blocks in the BlockChain object.
  */
class BlockchainIndex {
  /** A map from the block hash to a block descriptor.
    */
  private val blockByHash   = mutable.HashMap[Hash, BlockDescriptor]()
  private val blockByHeight = mutable.HashMap[Long,      BlockDescriptor]()

  /** Find a block descriptor by a block hash.
    *
    * @param blockHash The hash of the block to find.
    * @return The found block descriptor.
    */
  def findBlock(blockHash : Hash) : Option[BlockDescriptor] = {
    blockByHash.get(blockHash)
  }


  /** Find a block descriptor by the block height.
    *
    * @param blockHeight The height of the block to find.
    * @return The found block descriptor.
    */
  def findBlock(blockHeight : Long) : Option[BlockDescriptor] = {
    blockByHeight.get(blockHeight)
  }

  /** Put a block descriptor.
    *
    * @param blockHash The hash of the block.
    * @param blockDescriptor The block descriptor.
    */
  def putBlock(blockHash : Hash, blockDescriptor : BlockDescriptor) : Unit = {
    blockByHash.put(blockHash, blockDescriptor)
    blockByHeight.put(blockDescriptor.height, blockDescriptor)
  }
}
