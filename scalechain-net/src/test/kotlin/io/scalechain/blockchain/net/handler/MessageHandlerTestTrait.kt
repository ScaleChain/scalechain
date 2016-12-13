package io.scalechain.blockchain.net.handler

import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.net.PeerCommunicator
import io.scalechain.blockchain.net.PeerSet
import io.scalechain.blockchain.net.Peer

/**
  * Created by kangmo on 6/4/16.
  */
abstract class MessageHandlerTestTrait : BlockchainTestTrait() {

  lateinit var context : MessageHandlerContext
  lateinit var channel : EmbeddedChannel

  override fun beforeEach() {
    // initialization code.
    channel = EmbeddedChannel()
    context = context(channel)


    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // finalization code
    channel.close()
  }

  fun context(embeddedChannel: EmbeddedChannel) : MessageHandlerContext {
    val peerSet = PeerSet.create()
    val peer : Peer = peerSet.add(embeddedChannel)
    return MessageHandlerContext(peer, PeerCommunicator(peerSet) )
  }
}
