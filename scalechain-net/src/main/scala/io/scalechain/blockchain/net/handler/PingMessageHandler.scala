package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{ProtocolMessage, Ping}
import org.slf4j.LoggerFactory

/**
  * The message handler for Ping message.
  */
object PingMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(PingMessageHandler.getClass)

  /** Handle Ping message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param ping The ping message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, ping : Ping ) : Unit = {
    // TODO : Implement
  }
}
