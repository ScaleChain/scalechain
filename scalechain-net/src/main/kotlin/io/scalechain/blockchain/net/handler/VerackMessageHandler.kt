package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.{Verack, ProtocolMessage}
import org.slf4j.LoggerFactory

/**
  * The message handler for Verack message.
  */
object VerackMessageHandler {
  private lazy val logger = Logger( LoggerFactory.getLogger(VerackMessageHandler.getClass) )

  /** Handle Verack message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param message The message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, message : Verack ) : Unit {
    // TODO : Implement
  }
}
