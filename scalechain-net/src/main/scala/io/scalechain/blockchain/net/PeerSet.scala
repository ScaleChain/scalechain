package io.scalechain.blockchain.net

import java.net.{SocketAddress, InetAddress, InetSocketAddress}
import java.util.concurrent.LinkedBlockingQueue

import com.typesafe.scalalogging.Logger
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.{ChannelFuture, ChannelFutureListener, Channel}
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.channel.group.{ChannelGroupFuture, ChannelGroupFutureListener, ChannelGroup, DefaultChannelGroup}
import io.netty.util.concurrent.{ImmediateEventExecutor, DefaultEventExecutorGroup, EventExecutor, GlobalEventExecutor}
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.{StackUtil, CollectionUtil}
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

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
  private val logger = Logger( LoggerFactory.getLogger(classOf[PeerSet]) )

  /**
    * A channel group that has all connected channels.
    * Assumption : When the connection closes, the channel is removed from the channel group.
    */
  //val channels : ChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  val channels : ChannelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

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
          channels.add(channel)
          logger.trace(s"Added a peer to channel group. ${peer}")
          peer
        }
        // For unit tests. We need to accept a socket whose toString method returns "embedded"
        case embeddedSocketAddress : SocketAddress => {
          if ( embeddedSocketAddress .toString == "embedded") {
            val peer = Peer(channel)
            // Put the peer as a peer on the localhost using port 1000.
            peerByAddress.put(new InetSocketAddress(1000), peer )
            channels.add(channel)
            logger.trace(s"Added a peer to channel group. ${peer}")
            peer
          } else {
            val message = s"The remote address of the channel to add was not the type EmbeddedSocketAddress. Remote Address : ${channel.remoteAddress()}"
            logger.error(message)
            throw new ChainException(ErrorCode.InternalError, message )
          }
        }
        case _ => {
          val message = s"The remote address of the channel to add was not the type InetSocketAddress. Remote Address : ${channel.remoteAddress()}"
          logger.error(message)
          throw new ChainException(ErrorCode.InternalError, message )
        }
      }
    }
  }

  /**
    * Send a message to all connected peers.
 *
    * @param message The message to send.
    */
  def sendToAll(message : ProtocolMessage): Unit = {
    val messageString = MessageSummarizer.summarize(message)

    if (channels.size > 0) {
      logger.trace(s"Sending to all peers : ${messageString}")

      channels.writeAndFlush(message).addListener(new ChannelGroupFutureListener() {
        def operationComplete(future:ChannelGroupFuture) {
          assert( future.isDone )
          val remoteAddresses = channels.iterator().asScala.map(_.remoteAddress).mkString(",");
          if (future.isSuccess) { // completed successfully
            logger.debug(s"Successfully sent to peers : ${remoteAddresses}, ${messageString}")
          }

          if (future.cause() != null) { // completed with failure
          val failureDescriptions =
            future.cause.iterator.asScala.map{ entry =>
              val channel : Channel = entry.getKey
              val throwable : Throwable = entry.getValue
              val causeDescription =
                if (throwable.getCause == null)
                  ""
                else
                  s"Cause : { exception : ${throwable.getCause}, stack : ${StackUtil.getStackTrace(throwable.getCause)} }"
              s"An exception happened for remote address : ${channel.remoteAddress()}, Exception : ${throwable}, Stack Trace : ${StackUtil.getStackTrace(throwable)}. ${causeDescription}}"
            }.mkString("\n")

            logger.debug(s"Failed to send to (some of) peers : ${remoteAddresses}, detail : ${failureDescriptions}" )
          }

          if (future.isCancelled) { // completed by cancellation
            logger.debug(s"Canceled to send to peers : ${remoteAddresses}, ${messageString}")
          }
        }
      })
    } else {
      logger.warn(s"No connected peer to send the message : ${messageString}")
    }
  }


  /**
    * Remove a peer that matches the remote address.
    * Called when the connection to the peer closes.
 *
    * @param remoteAddress The remote address of the peer to remove.
    */
  def remove(remoteAddress : SocketAddress): Unit = {
    synchronized {
      // Note : nothing to do for the channels.
      // when a channel is disconnected, the channel is removed from channels group.

      remoteAddress match {
        case inetAddress : InetSocketAddress => {
          peerByAddress.remove(inetAddress)
        }
        // For unit tests. We need to accept a socket whose toString method returns "embedded"
        case embeddedSocketAddress : SocketAddress => {
          if ( embeddedSocketAddress.toString == "embedded") {
            peerByAddress.remove(new InetSocketAddress(1000) )
          } else {
            val message = s"The remote address of the channel to remove was not the type EmbeddedSocketAddress. Remote Address : ${remoteAddress}"
            logger.error(message)
            throw new ChainException(ErrorCode.InternalError, message )
          }
        }
        case _ => {
          val message = s"The remote address of the channel to remove was not the type InetSocketAddress or EmbeddedSocketAddress. Remote Address : ${remoteAddress}"
          logger.error(message)
          throw new ChainException(ErrorCode.InternalError, message )
        }
      }
    }
  }
/*
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
*/
  def all() : Iterable[Peer] = {
    synchronized {
      // BUGBUG : Make sure if it is safe to return an iterable from the synchronized block.
      peerByAddress.values.filter(_.isLive)
    }
  }

  def peers() : Iterable[(InetSocketAddress, Peer)] = {
    synchronized {
      for ((address, peer) <- peerByAddress ;
           if peer.isLive )
        yield (address, peer)
    }
  }

  /** See if a peer from the given address exists. This method does not check the port.
    *
    * @param address The address of the peer to find.
    * @return true if a peer from the address was found; false otherwise.
    */
  def hasPeer(address : InetSocketAddress) = {
    ! peers.filter { case (inetSocketAddress, peer) =>
      inetSocketAddress.getAddress == address && peer.isLive
    }.isEmpty
  }
}


