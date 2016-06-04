package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{Inv, ProtocolMessage}
import org.slf4j.LoggerFactory

/**
  * The message handler for Inv message.
  */
object InvMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(InvMessageHandler.getClass)

  /** Handle Inv message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param inv The Inv message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, inv : Inv ) : Option[ProtocolMessage] = {
    // TODO : Implement
    None
  }
}
