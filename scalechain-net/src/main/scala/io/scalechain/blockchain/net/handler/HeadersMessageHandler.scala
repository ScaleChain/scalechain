package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{ProtocolMessage, Headers}
import org.slf4j.LoggerFactory

/**
  * The message handler for Headers message.
  */
object HeadersMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(HeadersMessageHandler.getClass)

  /** Handle Headers message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param headers The Headers message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, headers : Headers ) : Option[ProtocolMessage] = {
    // TODO : Implement
    None
  }
}
