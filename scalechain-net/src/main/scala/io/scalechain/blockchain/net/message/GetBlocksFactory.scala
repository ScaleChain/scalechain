package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.{GetBlocks, Hash, BlockInfo}

/**
  *  The factory that creates GetBlocks messages.
  */
object GetBlocksFactory {
  /** Create a GetBlocks message to get the given block.
    *
    * @param blockHashToGet The hash of the block to get.
    * @return
    */
  def create(blockHashToGet : Hash) : GetBlocks = {
    // TODO : Implement
    assert(false)
    null
  }
}
