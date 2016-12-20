package io.scalechain.blockchain.net

import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue

import com.typesafe.scalalogging.Logger
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.Channel
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.Version
import io.scalechain.util.StackUtil
//import org.apache.commons.collections4.map.LRUMap
import org.slf4j.LoggerFactory


/** Represents a connected peer.
  *
  * @param channel The netty channel where we send messages.
  */
data class Peer(private val channel : Channel) {
  private val logger = LoggerFactory.getLogger(Peer::class.java)

  /**
    * The version we got from the peer. This is set to some value only if we received the Version message.
    */
  var versionOption : Version? = null
  var pongReceived : Int? = null

  /**
    * Update version received from the peer.
    *
    * @param version The version received from the peer.
    */
  fun updateVersion(version : Version) : Unit {
    versionOption = version
  }

  /** Return if this peer is live.
    *
    * @return True if this peer is live, false otherwise.
    */
  fun isLive() : Boolean {
    // TODO : Implement this based on the time we received pong from this peer.

    return channel.isOpen && channel.isActive
    // Check if the time we received pong is within a threshold.
    //assert(false)
//    true
  }

  fun send(message : ProtocolMessage) {
    val messageString = MessageSummarizer.summarize(message)
    channel.writeAndFlush(message).addListener(object : ChannelFutureListener {
      override fun operationComplete(future:ChannelFuture) {
        assert( future.isDone )
        if (future.isSuccess) { // completed successfully
          logger.debug("Successfully sent to peer : ${channel.remoteAddress()}, ${messageString}")
        }

        if (future.cause() != null) { // completed with failure
          logger.debug("Failed to send to peer : ${channel.remoteAddress()}, ${messageString}, Exception : ${future.cause().message}, Stack Trace : ${StackUtil.getStackTrace(future.cause())}")
        }

        if (future.isCancelled) { // completed by cancellation
          logger.debug("Canceled to send to peer : ${channel.remoteAddress()}, ${messageString}")
        }
      }
    })
  }
}


data class PeerInfo(
                     // (Since : 0.10.0) The node’s index number in the local node address database.
                     val id : Int, // 9
                     // The IP address and port number used for the connection to the remote node.
                     val addr : String, // "192.0.2.113:18333"
                     // Our IP address and port number according to the remote node. M
                     // May be incorrect due to error or lying. Many SPV nodes set this to 127.0.0.1:8333
                     //  addrlocal : Option<String>, // "192.0.2.51:18333"
                     // The services advertised by the remote node in its version message
                     //  services : String, // "0000000000000002"
                     // The Unix epoch time when we last successfully sent data to the TCP socket for this node
                     //  lastsend : Long, // 1419277992
                     // The Unix epoch time when we last received data from this node
                     //  lastrecv : Long, // 1419277992
                     // The total number of bytes we’ve sent to this node
                     //  bytessent : Long, // 4968
                     // The total number of bytes we’ve received from this node
                     //  bytesrecv : Long,  // 105078
                     // The Unix epoch time when we connected to this node
                     //  conntime : Long, // 1419265985
                     // The number of seconds this node took to respond to our last P2P ping message
                     //  pingtime : java.math.BigDecimal, // 0.05617800
                     // The number of seconds we’ve been waiting for this node to respond to a P2P ping message.
                     // Only shown if there’s an outstanding ping message
                     //  pingwait : Option<java.math.BigDecimal>, // 0.04847123
                     // The protocol version number used by this node. See the protocol versions section for more information
                     val version : Int?, // 70001
                     // The user agent this node sends in its version message.
                     // This string will have been sanitized to prevent corrupting the JSON results. May be an empty string
                     val subver : String?, // "/Satoshi:0.8.6/"
                     // Set to true if this node connected to us; set to false if we connected to this node
                     //  inbound : Boolean, // false
                     // The height of the remote node’s block chain when it connected to us as reported in its version message
                     val startingheight : Long? // 315280
                     // The ban score we’ve assigned the node based on any misbehavior it’s made.
                     // By default, Bitcoin Core disconnects when the ban score reaches 100
                     //  banscore : Int,  // 0
                     // ( Since : 0.10.0 ) The highest-height header we have in common with this node based the last P2P headers message it sent us.
                     // If a headers message has not been received, this will be set to -1
                     //  synced_headers : Long,  // -1
                     // ( Since : 0.10.0 ) The highest-height block we have in common with this node based on P2P inv messages this node sent us.
                     // If no block inv messages have been received from this node, this will be set to -1
                     //  synced_blocks : Long, // -1
                     // ( Since : 0.10.0 ) An array of blocks which have been requested from this peer. May be empty
                     // inflight item : The height of a block being requested from the remote peer.
                     //  inflight : List<Long>, // <>,
                     // ( Since : 0.10.0 )
                     // Set to true if the remote peer has been whitelisted; otherwise, set to false.
                     // Whitelisted peers will not be banned if their ban score exceeds the maximum (100 by default).
                     // By default, peers connecting from localhost are whitelisted
                     //  whitelisted : Boolean // false
                   ) {
  companion object {
    fun create(peerIndex : Int, remoteAddress : InetSocketAddress, peer : Peer) : PeerInfo {
      return PeerInfo(
        id=peerIndex,
        addr="${remoteAddress.getAddress().getHostAddress()}:${remoteAddress.getPort()}",
        version=peer.versionOption?.version,
        subver=peer.versionOption?.userAgent,
        startingheight = peer.versionOption?.startHeight?.toLong()
      )
    }
  }
}



