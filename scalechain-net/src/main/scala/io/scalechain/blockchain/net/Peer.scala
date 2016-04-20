package io.scalechain.blockchain.net

import java.util.concurrent.LinkedBlockingQueue

import io.scalechain.blockchain.proto.{ProtocolMessage, Version}
import org.slf4j.LoggerFactory


/** Represents a connected peer.
  * @param messageSendQueue The queue for sending messages to this peer. The queue is thread-safe.
  * @param versionOption The version we got from the peer. This is set to some value only if we received the Version message.
  */
case class Peer(private val messageSendQueue : LinkedBlockingQueue[ProtocolMessage], var versionOption : Option[Version] = None, var pongReceived : Option[Int] = None) {
  private val logger = LoggerFactory.getLogger(classOf[Peer])

  /** Return if this peer is live.
    *
    * @return True if this peer is live, false otherwise.
    */
  def isLive : Boolean = {
    // TODO : Implement this based on the time we received pong from this peer.
    // Check if the time we received pong is within a threshold.
    //assert(false)
    true
  }

  def send(message : ProtocolMessage) = {
    if ( ! messageSendQueue.offer(message) ) {
      logger.error(s"The send queue of peer is full. Peer : ${this}")
    }
  }
}
