package io.scalechain.blockchain.net

import java.net.SocketAddress
import java.net.InetSocketAddress
import io.netty.channel.Channel
import io.netty.channel.group.ChannelGroupFuture
import io.netty.channel.group.ChannelGroupFutureListener
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.ImmediateEventExecutor
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.StackUtil
import org.slf4j.LoggerFactory




/** The set of peers populated as we have a connection to a new peer.
  * Also, we need to remove a peer from the peer set if the peer was disconnected.
  *
  * This class should be thread-safe.
  * Why? We have a stream running for each TCP connection to a peer.
  * Each stream needs to access the peer set to register a new peer, send messages to all peers, etc.
  * As a stream is materialized using Akka actors, multiple threads can run at the same time trying to access the peer set.
  */
class PeerSet {
  private val logger = LoggerFactory.getLogger(PeerSet::class.java)

  /**
    * A channel group that has all connected channels.
    * Assumption : When the connection closes, the channel is removed from the channel group.
    */
  //val channels : ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  val channels : ChannelGroup = DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

  val peerByAddress = mutableMapOf<InetSocketAddress, Peer>()

/*
  fun getPeerByAddress(connectedPeer:Peer, remotePeerAddress : InetSocketAddress) : Peer {
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
  /** Add a peer connected via the given channel.
    *
    * @param channel The connected channel.
    * @return
    */
  fun add(channel : Channel) : Peer {
    synchronized(this) {
      val remoteAddress = channel.remoteAddress()
      when {
        remoteAddress is InetSocketAddress -> {
          val peer = Peer(channel)
          peerByAddress.put(remoteAddress, peer )
          channels.add(channel)
          logger.trace("Added a peer to channel group. ${peer}")
          return peer
        }
        // For unit tests. We need to accept a socket whose toString method returns "embedded"
        remoteAddress is SocketAddress -> {
          if ( remoteAddress.toString() == "embedded") {
            val peer = Peer(channel)
            // Put the peer as a peer on the localhost using port 1000.
            peerByAddress.put(InetSocketAddress(1000), peer )
            channels.add(channel)
            logger.trace("Added a peer to channel group. ${peer}")
            return peer
          } else {
            val message = "The remote address of the channel to add was not the type EmbeddedSocketAddress. Remote Address : ${channel.remoteAddress()}"
            logger.error(message)
            throw ChainException(ErrorCode.InternalError, message )
          }
        }
        else -> {
          val message = "The remote address of the channel to add was not the type InetSocketAddress. Remote Address : ${channel.remoteAddress()}"
          logger.error(message)
          throw ChainException(ErrorCode.InternalError, message )
        }
      }
    }
  }

  /**
    * Send a message to all connected peers.
 *
    * @param message The message to send.
    */
  fun sendToAll(message : ProtocolMessage): Unit {
    synchronized(this) {
      val messageString = MessageSummarizer.summarize(message)

      if (channels.size > 0) {
        logger.trace("Sending to all peers : ${messageString}")

        channels.writeAndFlush(message).addListener(object : ChannelGroupFutureListener {
          override fun operationComplete(future: ChannelGroupFuture) {
            assert(future.isDone)
            val remoteAddresses = channels.iterator().asSequence().map{it.remoteAddress()}.joinToString(",");
            if (future.isSuccess) {
              // completed successfully
              logger.debug("Successfully sent to peers : ${remoteAddresses}, ${messageString}")
            }

            if (future.cause() != null) {
              // completed with failure
              val failureDescriptions =
                future.cause().iterator().asSequence().map { entry ->
                  val channel: Channel = entry.key
                  val throwable: Throwable = entry.value
                  val causeDescription =
                    if (throwable.cause == null)
                      ""
                    else
                      "Cause : { exception : ${throwable.cause!!}, stack : ${StackUtil.getStackTrace(throwable.cause!!)} }"
                  "An exception happened for remote address : ${channel.remoteAddress()}, Exception : ${throwable}, Stack Trace : ${StackUtil.getStackTrace(throwable)}. ${causeDescription}}"
                }.joinToString("\n")

              logger.debug("Failed to send to (some of) peers : ${remoteAddresses}, detail : ${failureDescriptions}")
            }

            if (future.isCancelled) {
              // completed by cancellation
              logger.debug("Canceled to send to peers : ${remoteAddresses}, ${messageString}")
            }
          }
        })
      } else {
        logger.warn("No connected peer to send the message : ${messageString}")
      }
    }
  }


  /**
    * Remove a peer that matches the remote address.
    * Called when the connection to the peer closes.
 *
    * @param remoteAddress The remote address of the peer to remove.
    */
  fun remove(remoteAddress : SocketAddress): Unit {
    synchronized(this) {
      // Note : nothing to do for the channels.
      // when a channel is disconnected, the channel is removed from channels group.

      when {
        remoteAddress is InetSocketAddress -> {
          peerByAddress.remove(remoteAddress)
        }
        // For unit tests. We need to accept a socket whose toString method returns "embedded"
        remoteAddress is SocketAddress -> {
          if ( remoteAddress.toString() == "embedded") {
            peerByAddress.remove(InetSocketAddress(1000) )
          } else {
            val message = "The remote address of the channel to remove was not the type EmbeddedSocketAddress. Remote Address : ${remoteAddress}"
            logger.error(message)
            throw ChainException(ErrorCode.InternalError, message )
          }
        }
        else -> {
          val message = "The remote address of the channel to remove was not the type InetSocketAddress or EmbeddedSocketAddress. Remote Address : ${remoteAddress}"
          logger.error(message)
          throw ChainException(ErrorCode.InternalError, message )
        }
      }
    }
  }
/*
  fun any() : Option<Peer> {
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
  fun all() : Iterable<Peer> {
    synchronized(this) {
      // BUGBUG : Make sure if it is safe to return an iterable from the synchronized block.
      return peerByAddress.values.filter{it.isLive()}
    }
  }

  fun peers() : Iterable<Pair<InetSocketAddress, Peer>> {
    synchronized(this) {
      return peerByAddress.map { entry ->
        val address = entry.key
        val peer = entry.value
        if (peer.isLive())
          Pair(address,peer)
        else
          null
      }.filterNotNull()
    }
  }

  /** See if a peer from the given address exists. This method does not check the port.
    *
    * @param address The address of the peer to find.
    * @return true if a peer from the address was found; false otherwise.
    */
  fun hasPeer(address : InetSocketAddress) : Boolean {
    return peers().filter { pair ->
      val inetSocketAddress = pair.first
      val peer = pair.second
      return inetSocketAddress.getAddress() == address && peer.isLive()
    }.isNotEmpty()
  }

  companion object {
    var thePeerSet : PeerSet? = null
    fun create() : PeerSet {
      if (thePeerSet == null)
        thePeerSet = PeerSet()
      return thePeerSet!!
    }
    fun get() : PeerSet = thePeerSet!!
  }
}


