package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{Props, ActorRef, Actor}
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.CollectionUtil
import scala.collection.mutable

object PeerBroker {
  val props = Props[PeerBroker]
  case class SendToAny(message : ProtocolMessage)
  case class SendToAll(message : ProtocolMessage)
}

/** Forwards a message to a Peer based on the remote address of the peer.
 * - A peer broker knows which peer is for a specific remote address.
 * - The peer broker forwards messages to a peer based on the remote address of the peer.
 */
class PeerBroker extends Actor {
  val peerByAddress = mutable.HashMap[InetSocketAddress, Peer]()

  def anyLivePeer() : Peer = {
    val livePeers = peerByAddress.values.filter(_.messageTransformer.isLive)
    val randomLivePeer = CollectionUtil.random(livePeers)
    randomLivePeer
  }

  def getPeerByAddress(connectedPeer:Peer, remotePeerAddress : InetSocketAddress) : Peer = {
    println("PeerBroker.getPeerByAddress")
    val peer = peerByAddress.get(remotePeerAddress) match {
      case Some(foundPeer : Peer) => foundPeer
      case None => {
        println("PeerBroker:None")
        peerByAddress.put(remotePeerAddress, connectedPeer)
        connectedPeer
      }
    }
    peer
  }

  import PeerBroker._

  def receive = {
    // BUGBUG : Change to case class
    case (connectedPeer:Peer, remotePeerAddress:InetSocketAddress, protocolMessageOption:Option[ProtocolMessage]) => {
      println("PeerBroker.receive")

      // Create the peer node which is connected to the remote peer address.
      val peer = getPeerByAddress(connectedPeer, remotePeerAddress)

      // forward a message if any.
      protocolMessageOption.map { protocolMessage =>
        peer.requester forward protocolMessage
      }
    }
    case SendToAny(message) => anyLivePeer.requester ! message
    case SendToAll(message) => {
      peerByAddress.values.filter(_.messageTransformer.isLive).map { peer =>
        peer.requester ! message
      }
    }
  }
}
