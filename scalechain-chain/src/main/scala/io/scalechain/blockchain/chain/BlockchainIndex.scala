package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.BlockHash

import scala.collection.mutable

/** An index for looking up transactions and blocks in the BlockChain object.
  */
class BlockchainIndex {
  /** A map from the block hash to a block descriptor.
    */
  private val blockByHash = mutable.HashMap[BlockHash, BlockDescriptor]()

  /** Find a block descriptor by a block hash.
    *
    * @param blockHash The hash of the block to find.
    * @return The found block descriptor.
    */
  def findBlock(blockHash : BlockHash) : Option[BlockDescriptor] = {
    blockByHash.get(blockHash)
  }

  /** Put a block descriptor.
    *
    * @param blockHash The hash of the block.
    * @param blockDescriptor The block descriptor.
    */
  def putBlock(blockHash : BlockHash, blockDescriptor : BlockDescriptor) : Unit = {
    blockByHash.put(blockHash, blockDescriptor)
  }
}
