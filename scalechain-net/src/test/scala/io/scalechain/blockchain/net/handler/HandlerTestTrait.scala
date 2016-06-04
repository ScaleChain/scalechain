package io.scalechain.blockchain.net.handler

import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.net.{PeerCommunicator, PeerSet, Peer}

/**
  * Created by kangmo on 6/4/16.
  */
trait HandlerTestTrait {
  def context(embeddedChannel: EmbeddedChannel) = {
    val peerSet = PeerSet.create
    val peer : Peer = peerSet.add(embeddedChannel)
    new MessageHandlerContext(peer, new PeerCommunicator(peerSet) )
  }
}
