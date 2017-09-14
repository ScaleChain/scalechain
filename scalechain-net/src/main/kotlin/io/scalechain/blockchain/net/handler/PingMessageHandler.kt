package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.net.controller.MessageHandler
import io.scalechain.blockchain.proto.Ping
import org.slf4j.LoggerFactory

/**
  * The message handler for Ping message.
  */
object PingMessageHandler : MessageHandler<Ping> {
  private val logger = LoggerFactory.getLogger(PingMessageHandler.javaClass)

  /** Handle Ping message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param ping The ping message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  override fun handle( context : MessageHandlerContext, ping : Ping ) : Boolean {
    // TODO : Implement
    return false
  }
}
