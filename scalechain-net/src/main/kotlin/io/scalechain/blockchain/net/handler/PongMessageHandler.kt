package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.net.controller.MessageHandler
import io.scalechain.blockchain.proto.Pong
import org.slf4j.LoggerFactory

/**
  * The message handler for Pong message.
  */
object PongMessageHandler : MessageHandler<Pong> {
  private val logger = LoggerFactory.getLogger(PongMessageHandler.javaClass)

  /** Handle Pong message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param pong The Pong message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  override fun handle( context : MessageHandlerContext, pong : Pong ) : Boolean {
    // TODO : Implement
    return false
  }
}
