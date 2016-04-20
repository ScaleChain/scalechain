package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto._
import org.slf4j.LoggerFactory

class ProtocolMessageHandler  {
  private val logger = LoggerFactory.getLogger(classOf[ProtocolMessageHandler])

  /** Handle a message coming from the TCP stream.
    *
    * @param message The messages to handle.
    * @return The list of responses we created after handling each message in messages.
    */
  def handle(message : ProtocolMessage): Option[ProtocolMessage] = {
    // Return Some[ProtocolMessage] if we need to reply a message. Return None otherwise.
    message match {
      case version: Version => {
        assert(false)
        // TODO : Implement - Update peerInfo.version.
        Some(Verack())
      }
      case Ping(nonce) => {
        assert(false)
        // TODO : Implement - Update peerInfo.lastPingReceivedTime.
        Some(Pong(nonce))
      }
      case Pong(nonce) => {
        assert(false)
        // TODO : Implement - Update peerInfo.lastPongReceivedTime.
        None
      }
      case verack: Verack => {
        assert(false)
        // TODO : Implement - Handler for a verack.
        None
      }
      case addr: Addr => {
        assert(false)
        // TODO : Implement - Handler for an addr.
        None
      }
      case inv: Inv => {
        assert(false)
        // TODO : Implement - Handler for an inv.
        None
      }
      case headers: Headers => {
        assert(false)
        // TODO : Implement - Handler for headers.
        None
      }
      case transaction: Transaction => {
        assert(false)
        // TODO : Implement - Handler for a transaction.
        None
      }
      case block: Block => {
        assert(false)
        // TODO : Implement - Handler for a block.
        None
      }
      case m: ProtocolMessage => {
        logger.info("Received a message, but done nothing : " + m.getClass.getName)
        None
      }
    }
  }
}
