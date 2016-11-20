package io.scalechain.blockchain.net

import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue

import com.typesafe.scalalogging.Logger
import io.netty.channel.{ChannelFuture, ChannelFutureListener, Channel}
import io.scalechain.blockchain.proto.{Hash, ProtocolMessage, Version}
import io.scalechain.util.StackUtil
//import org.apache.commons.collections4.map.LRUMap
import org.slf4j.LoggerFactory


/** Represents a connected peer.
  *
  * @param channel The netty channel where we send messages.
  */
case class Peer(private val channel : Channel) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[Peer]) )

  /**
    * The version we got from the peer. This is set to some value only if we received the Version message.
    */
  var versionOption : Option[Version] = None
  var pongReceived : Option[Int] = None

  /**
    * Update version received from the peer.
    *
    * @param version The version received from the peer.
    */
  def updateVersion(version : Version) : Unit = {
    versionOption = Some(version)
  }

  /**
    * Keep block hashes requested using getblocks message.
    * Only keep up to 4 block hashes in the cache.
    */
  //protected[net] val blocksRequested = new LRUMap[Hash, Unit](4)
  var requestedBlockHashOption : Option[Hash] = None
  /**
    * Keep a block as requested using getblocks message.
 *
    * @param blockHash The hash of block requested using getblocks message.
    */
  def blockRequested(blockHash : Hash) : Unit = {
    requestedBlockHashOption = Some(blockHash)
  }

  /**
    * Check if a block was requested using getblocks message.
 *
    * @param blockHash The block hash to check.
    * @return true if the block was requested; false otherwise.
    */
  def isBlockRequested(blockHash : Hash) = {
    requestedBlockHashOption == Some(blockHash)
  }

  def requestedBlock() = requestedBlockHashOption

  /**
    * Clear all requested blocks.
    */
  def clearRequestedBlock() = {
    requestedBlockHashOption = None
  }



  /** Return if this peer is live.
    *
    * @return True if this peer is live, false otherwise.
    */
  def isLive : Boolean = {
    // TODO : Implement this based on the time we received pong from this peer.
    channel.isOpen && channel.isActive
    // Check if the time we received pong is within a threshold.
    //assert(false)
//    true
  }

  def send(message : ProtocolMessage) = {
    val messageString = MessageSummarizer.summarize(message)
    channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
      def operationComplete(future:ChannelFuture) {
        assert( future.isDone )
        if (future.isSuccess) { // completed successfully
          logger.debug(s"Successfully sent to peer : ${channel.remoteAddress}, ${messageString}")
        }

        if (future.cause() != null) { // completed with failure
          logger.debug(s"Failed to send to peer : ${channel.remoteAddress}, ${messageString}, Exception : ${future.cause.getMessage}, Stack Trace : ${StackUtil.getStackTrace(future.cause())}")
        }

        if (future.isCancelled) { // completed by cancellation
          logger.debug(s"Canceled to send to peer : ${channel.remoteAddress}, ${messageString}")
        }
      }
    })
  }
}


case class PeerInfo(
                     // (Since : 0.10.0) The node’s index number in the local node address database.
                     id : Int, // 9
                     // The IP address and port number used for the connection to the remote node.
                     addr : String, // "192.0.2.113:18333"
                     // Our IP address and port number according to the remote node. M
                     // May be incorrect due to error or lying. Many SPV nodes set this to 127.0.0.1:8333
                     //  addrlocal : Option[String], // "192.0.2.51:18333"
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
                     //  pingtime : scala.math.BigDecimal, // 0.05617800
                     // The number of seconds we’ve been waiting for this node to respond to a P2P ping message.
                     // Only shown if there’s an outstanding ping message
                     //  pingwait : Option[scala.math.BigDecimal], // 0.04847123
                     // The protocol version number used by this node. See the protocol versions section for more information
                     version : Option[Int], // 70001
                     // The user agent this node sends in its version message.
                     // This string will have been sanitized to prevent corrupting the JSON results. May be an empty string
                     subver : Option[String], // "/Satoshi:0.8.6/"
                     // Set to true if this node connected to us; set to false if we connected to this node
                     //  inbound : Boolean, // false
                     // The height of the remote node’s block chain when it connected to us as reported in its version message
                     startingheight : Option[Long] // 315280
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
                     //  inflight : List[Long], // [],
                     // ( Since : 0.10.0 )
                     // Set to true if the remote peer has been whitelisted; otherwise, set to false.
                     // Whitelisted peers will not be banned if their ban score exceeds the maximum (100 by default).
                     // By default, peers connecting from localhost are whitelisted
                     //  whitelisted : Boolean // false
                   )


object PeerInfo {
  def create(peerIndex : Int, remoteAddress : InetSocketAddress, peer : Peer) : PeerInfo = {
    PeerInfo(
      id=peerIndex,
      addr=s"${remoteAddress.getAddress.getHostAddress}:${remoteAddress.getPort}",
      version=peer.versionOption.map(_.version),
      subver=peer.versionOption.map(_.userAgent),
      startingheight = peer.versionOption.map(_.startHeight)
    )
  }
}

