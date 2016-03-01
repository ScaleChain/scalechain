package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Block, BlockHash, Hash}

// [ Storage layer ] Maintains block chains with different height, it knows which one is the best one.
class Blockchain {
  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  def getBestBlockHash : Hash = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : BlockHash ) : Block = {
    // TODO : Implement
    assert(false)
    null
  }
}
