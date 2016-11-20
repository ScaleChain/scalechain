package io.scalechain.blockchain.net.handler

import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.net.{PeerCommunicator, PeerSet, Peer}

/**
  * Created by kangmo on 6/4/16.
  */
trait MessageHandlerTestTrait extends BlockchainTestTrait {

  var context : MessageHandlerContext = null
  var channel : EmbeddedChannel = null

  override def beforeEach() {
    // initialization code.
    channel = new EmbeddedChannel()
    context = context(channel)


    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalization code
    context = null
    channel.close()
  }

  def context(embeddedChannel: EmbeddedChannel) = {
    val peerSet = PeerSet.create
    val peer : Peer = peerSet.add(embeddedChannel)
    new MessageHandlerContext(peer, new PeerCommunicator(peerSet) )
  }
}
