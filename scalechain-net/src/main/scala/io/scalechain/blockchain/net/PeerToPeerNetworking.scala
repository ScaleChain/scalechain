package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

case class PeerAddress(address : String, port : Int)

object PeerToPeerNetworking {
  def getPeerCommunicator(inboundPort : Int, peerAddresses : List[PeerAddress],  system : ActorSystem, materializer : ActorMaterializer ) : PeerCommunicator = {
    // The peer set that keeps multiple PeerNode(s).
    val peerSet = PeerSet.create

    // The consumer that opens an inbound port, and waits for connections from other peers.
    val server = StreamServerLogic(system, materializer, peerSet, new InetSocketAddress("127.0.0.1", inboundPort))

    peerAddresses.map { peer =>
      val peerAddress = new InetSocketAddress(peer.address, peer.port)
      val client = StreamClientLogic(system, materializer, peerSet, peerAddress)

      // Send StartPeer message to the peer, so that it can initiate the node start-up process.
      //peerBroker ! (clientProducer /*connected peer*/, peerAddress, Some(StartPeer) /* No message to send to the peer node */  )
    }

    new PeerCommunicator(peerSet)
  }
}
