package io.scalechain.blockchain.net

import java.net.{SocketAddress, InetAddress, InetSocketAddress}
import java.util.concurrent.LinkedBlockingQueue

import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.{ErrorCode, ChainException}
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
  private val logger = LoggerFactory.getLogger(classOf[PeerSet])

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
  /** Add a new peer connected via the given channel.
    *
    * @param channel The connected channel.
    * @return
    */
  def add(channel : Channel) : Peer = {
    synchronized {
      channel.remoteAddress() match {
        case inetAddress : InetSocketAddress => {
          val peer = Peer(channel)
          peerByAddress.put(inetAddress, peer )
          peer
        }
        // For unit tests. We need to accept a socket whose toString method returns "embedded"
        case embeddedSocketAddress : SocketAddress => {
          if ( embeddedSocketAddress .toString == "embedded") {
            val peer = Peer(channel)
            // Put the peer as a peer on the localhost using port 1000.
            peerByAddress.put(new InetSocketAddress(1000), peer )
            peer
          } else {
            val message = s"The remote address of the channel was not the type EmbeddedSocketAddress. Remote Address : ${channel.remoteAddress()}"
            logger.error(message)
            throw new ChainException(ErrorCode.InternalError, message )
          }
        }
        case _ => {
          val message = s"The remote address of the channel was not the type InetSocketAddress. Remote Address : ${channel.remoteAddress()}"
          logger.error(message)
          throw new ChainException(ErrorCode.InternalError, message )
        }
      }
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
    synchronized {
      for ((address, peer) <- peerByAddress)
        yield (address, peer)
    }
  }

  /** See if a peer from the given address exists. This method does not check the port.
    *
    * @param address The address of the peer to find.
    * @return true if a peer from the address was found; false otherwise.
    */
  def hasPeer(address : InetSocketAddress) = {
    ! peers.filter { case (inetSocketAddress, _) =>
      inetSocketAddress.getAddress == address
    }.isEmpty
  }
}


