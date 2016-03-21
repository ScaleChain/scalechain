package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import io.scalechain.blockchain.net.DomainMessageRouter.VersionFrom
import io.scalechain.blockchain.net.service.PeerInfo
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.CollectionUtil
import org.slf4j.LoggerFactory
import scala.collection.mutable

object PeerBroker {
  val props = Props[PeerBroker]
  case class SendToAny(message : ProtocolMessage)
  case class SendToAll(message : ProtocolMessage)
  case class SendToOne(address : InetSocketAddress, message : ProtocolMessage)
  case class RegisterPeer(connectedPeer:Peer, remotePeerAddress:InetSocketAddress, protocolMessageOption:Option[ProtocolMessage])

  case class GetPeerInfo()
  case class GetPeerInfoResult(peerInfos : List[PeerInfo])


  var thePeerBroker : ActorRef = null

  def create(system : ActorSystem) : ActorRef = {
    assert(thePeerBroker == null)
    thePeerBroker = system.actorOf(PeerBroker.props, "peerBroker")
    thePeerBroker
  }

  /** Get the peer broker actor. This actor is a singleton, used by API layer to access block database.
    *
    * @return The peer broker actor.
    */
  def get() : ActorRef = {
    assert(thePeerBroker != null)
    thePeerBroker
  }
}

/** Forwards a message to a Peer based on the remote address of the peer.
 * - A peer broker knows which peer is for a specific remote address.
 * - The peer broker forwards messages to a peer based on the remote address of the peer.
 */
class PeerBroker extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[PeerBroker])

  val peerByAddress = mutable.HashMap[InetSocketAddress, Peer]()

  def anyLivePeer() : Option[Peer] = {
    val livePeers = peerByAddress.values.filter(_.messageTransformer.isLive)
    if (livePeers.isEmpty) {
      None
    } else {
      val randomLivePeer = CollectionUtil.random(livePeers)
      Some(randomLivePeer)
    }
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

  def getPeerInfo(peerIndex : Int, remoteAddress : InetSocketAddress, peer : Peer) : PeerInfo = {
    PeerInfo(
      id=peerIndex,
      addr=s"${remoteAddress.getAddress.getHostAddress}:${remoteAddress.getPort}",
      version=peer.versionOption.map(_.version),
      subver=peer.versionOption.map(_.userAgent)
    )
  }

  def receive = {

    case VersionFrom(address, version) => {
      // TODO : What happens if a peer has been disconnected?
      peerByAddress.get(address) match {
        case Some(peer) => {
          peer.versionOption = Some(version)
        }
        case None => {
          println(s"Got version from the peer($address), but the peer was not found by the peer broker.")
          logger.warn(s"Got version from the peer($address), but the peer was not found by the peer broker.")
        }
      }
    }

    case GetPeerInfo() => {
      var peerIndex = 0;

      val peerInfosIter = for (
        (address, peer) <- peerByAddress
      ) yield {
        peerIndex += 1
        getPeerInfo(peerIndex, address, peer)
      }

      val peerInfos : List[PeerInfo] = peerInfosIter.toList

      sender ! GetPeerInfoResult(peerInfos)
    }

    case RegisterPeer(connectedPeer, remotePeerAddress, protocolMessageOption) => {
      println("PeerBroker.receive")

      // Create the peer node which is connected to the remote peer address.
      val peer = getPeerByAddress(connectedPeer, remotePeerAddress)

      // forward a message if any.
      protocolMessageOption.map { protocolMessage =>
        peer.requester forward protocolMessage
      }
    }
    case SendToOne(address, message) => {
      peerByAddress.get(address) match {
        case Some(peer) => {
          peer.requester forward message
        }
        case None => {
          println(s"Got request to send a message($message) to the peer($address), but the peer was not found by the peer broker.")
          logger.warn(s"Got request to send a message($message) to the peer($address), but the peer was not found by the peer broker.")
        }
      }
    }
    case SendToAny(message) => {
      anyLivePeer match {
        case None => {
          // TODo : Implement.
          //assert(false);
          //
          println("PeerBroker:No live peer, Unable to send message:" + message)
        }
        case Some(livePeer) => {
          livePeer.requester ! message
        }
      }
    }
    case SendToAll(message) => {
      peerByAddress.values.filter(_.messageTransformer.isLive).map { peer =>
        peer.requester ! message
      }
    }
  }
}
