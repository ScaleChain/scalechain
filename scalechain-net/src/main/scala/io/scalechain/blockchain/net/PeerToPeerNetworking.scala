package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.netty.channel.ChannelFuture
import io.scalechain.blockchain.net.p2p.RetryingConnector
import io.scalechain.blockchain.proto.{IPv6Address, NetworkAddress, Version}
import io.scalechain.util.HexUtil._

import scala.concurrent.{ExecutionContext, Future}

case class PeerAddress(address : String, port : Int)

object PeerToPeerNetworking {
  def getPeerCommunicator(inboundPort : Int, peerAddresses : List[PeerAddress] ) : PeerCommunicator = {

    // The peer set that keeps multiple PeerNode(s).
    val peerSet = PeerSet.create

    val bindChannelFuture : ChannelFuture = new NodeServer(peerSet).listen(inboundPort)
    // Wait until the inbound port is bound.
    bindChannelFuture.sync()

    peerAddresses.map { peer =>
      new RetryingConnector(peerSet, retryIntervalSeconds=1).connect(peer.address, peer.port)
    }

    new PeerCommunicator(peerSet)
  }
}
