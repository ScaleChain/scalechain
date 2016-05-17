package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Block

/** A block in a best blockchain.
  * Has all data of a block and also additional information such as the height of a block.
  *
  * @param height The height of the block in the best blockchain.
  * @param block The block itself.
  */
case class ChainBlock (
                        height : Long,
                        block : Block
                      )

/** Provides an iterator that iterates each ChainBlock from a specific height. height 0 means the genesis block.
  */
trait BlockchainIteratorProvider {
  /** Return an iterator that iterates each ChainBlock.
    *
    * @param height Specifies where we start the iteration. The height 0 means the genesis block.
    * @return The iterator that iterates each ChainBlock.
    */
  def getIterator(height : Long) : Iterator[ChainBlock]
}
