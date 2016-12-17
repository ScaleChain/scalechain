package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.Config

/**
 * Created by kangmo on 17/12/2016.
 */
object BlockPropagator {
  fun propagate(blockHash : Hash, block : Block) : Unit {
    PeerToPeerNetworking.getPeerCommunicator().propagateBlock(block)

    BlockGateway.putReceivedBlock(blockHash, block)

    // If only one node is in the blockchain, we are running as single node mode.
    // Put consensual header immediately in this case, as we have no peer nodes to have consensus on the mined block.
    if (Config.peerAddresses().size == 1) {
      BlockGateway.putConsensualHeader(block.header)
    }

    BlockBroadcaster.get().broadcastHeader(block.header)
  }
}