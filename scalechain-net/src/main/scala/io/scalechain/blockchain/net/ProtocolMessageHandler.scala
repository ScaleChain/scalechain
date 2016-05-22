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
        // TODO : Implement - Update peerInfo.version.
        Some(Verack())
      }
      case Ping(nonce) => {
        // TODO : Implement - Update peerInfo.lastPingReceivedTime.
        Some(Pong(nonce))
      }
      case Pong(nonce) => {
        // TODO : Implement - Update peerInfo.lastPongReceivedTime.
        None
      }
      case verack: Verack => {
        // TODO : Implement - Handler for a verack.
        None
      }
      case addr: Addr => {
        // TODO : Implement - Handler for an addr.
        None
      }
      case inv: Inv => {
        // TODO : Implement - Handler for an inv.
        None
      }
      case headers: Headers => {
        // TODO : Implement - Handler for headers.
        None
      }
      case transaction: Transaction => {
        // TODO : Implement - Handler for a transaction.
        None
      }
      case block: Block => {
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
