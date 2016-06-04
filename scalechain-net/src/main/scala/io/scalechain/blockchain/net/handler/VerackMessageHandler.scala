package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{Verack, ProtocolMessage}
import org.slf4j.LoggerFactory

/**
  * The message handler for Verack message.
  */
object VerackMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(VerackMessageHandler.getClass)

  /** Handle Verack message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param message The message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, message : Verack ) : Option[ProtocolMessage] = {
    // TODO : Implement
    None
  }
}
