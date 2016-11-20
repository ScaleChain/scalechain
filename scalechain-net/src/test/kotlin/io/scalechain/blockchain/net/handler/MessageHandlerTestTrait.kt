package io.scalechain.blockchain.net.handler

import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.net.{PeerCommunicator, PeerSet, Peer}

/**
  * Created by kangmo on 6/4/16.
  */
trait MessageHandlerTestTrait : BlockchainTestTrait {

  var context : MessageHandlerContext = null
  var channel : EmbeddedChannel = null

  override fun beforeEach() {
    // initialization code.
    channel = EmbeddedChannel()
    context = context(channel)


    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // finalization code
    context = null
    channel.close()
  }

  fun context(embeddedChannel: EmbeddedChannel) {
    val peerSet = PeerSet.create
    val peer : Peer = peerSet.add(embeddedChannel)
    MessageHandlerContext(peer, PeerCommunicator(peerSet) )
  }
}
