package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{GetHeaders, ProtocolMessage, Block}
import org.slf4j.LoggerFactory

/**
  * The message handler for GetHeaders message.
  */
object GetHeadersMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(GetHeadersMessageHandler.getClass)

  /** Handle GetHeaders message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param getHeaders The GetHeaders message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, getHeaders : GetHeaders ) : Option[ProtocolMessage] = {
    // TODO : Implement
    None
  }
}
