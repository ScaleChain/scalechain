package io.scalechain.blockchain.net

import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.CollectionUtil
import org.slf4j.LoggerFactory

import scala.collection.mutable

object PeerSet {
  var thePeerSet : PeerSet = null
  def create : PeerSet = {
    if (thePeerSet == null)
      thePeerSet = new PeerSet()
    thePeerSet
  }
  def get : PeerSet = thePeerSet
}



/** The set of peers populated as we have a new connection to a new peer.
  * Also, we need to remove a peer from the peer set if the peer was disconnected.
  *
  * This class should be thread-safe.
  * Why? We have a stream running for each TCP connection to a peer.
  * Each stream needs to access the peer set to register a new peer, send messages to all peers, etc.
  * As a stream is materialized using Akka actors, multiple threads can run at the same time trying to access the peer set.
  */
class PeerSet {
  val peerByAddress = mutable.HashMap[InetSocketAddress, Peer]()

/*
  def getPeerByAddress(connectedPeer:Peer, remotePeerAddress : InetSocketAddress) : Peer = {
    synchronized {
      val peer = peerByAddress.get(remotePeerAddress) match {
        case Some(foundPeer : Peer) => foundPeer
        case None => {
          peerByAddress.put(remotePeerAddress, connectedPeer)
          connectedPeer
        }
      }
      peer
    }
  }
*/
  def add(remoteAddress : InetSocketAddress, sendQueue : ConcurrentLinkedQueue[ProtocolMessage]) = {
    synchronized {
      // BUGBUG : Make sure if it is ok to overwrite the original (existing) peer with the new peer.
      peerByAddress.put(remoteAddress, Peer(sendQueue) )
    }
  }

  def remove(remoteAddress : InetSocketAddress): Unit = {
    synchronized {
      peerByAddress.remove(remoteAddress)
    }
  }

  def any() : Option[Peer] = {
    synchronized{
      val livePeers = peerByAddress.values.filter(_.isLive)
      if (livePeers.isEmpty) {
        None
      } else {
        val randomLivePeer = CollectionUtil.random(livePeers)
        Some(randomLivePeer)
      }
    }
  }

  def all() : Iterable[Peer] = {
    synchronized {
      // BUGBUG : Make sure if it is safe to return an iterable from the synchronized block.
      peerByAddress.values.filter(_.isLive)
    }
  }

  def peers() : Iterable[(InetSocketAddress, Peer)] = {
    for ( (address,peer) <- peerByAddress)
      yield (address,peer)
  }
}


