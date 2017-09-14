package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.net.controller.MessageHandler
import io.scalechain.blockchain.proto.Addr
import org.slf4j.LoggerFactory

/**
  * The message handler for Addr message.
  */
object AddrMessageHandler : MessageHandler<Addr> {
  private val logger = LoggerFactory.getLogger(AddrMessageHandler.javaClass)

  /** Handle Addr message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param addr The Addr message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  override fun handle( context : MessageHandlerContext, addr : Addr ) : Boolean {
    // TODO : Implement
    return false
  }
}
