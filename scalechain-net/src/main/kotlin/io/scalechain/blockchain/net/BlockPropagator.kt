package io.scalechain.blockchain.net

import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Hash

/**
 * Created by kangmo on 17/12/2016.
 */
object BlockPropagator {
  fun propagate(blockHash : Hash, block : Block) : Unit {

    // When only one node is running, there is no peer. Need to put the block into the blockchain.
    BlockProcessor.get().acceptBlock(Hash(blockHash.value), block)
    PeerToPeerNetworking.getPeerCommunicator().propagateBlock(block)
  }
}