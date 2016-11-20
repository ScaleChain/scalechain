package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.{Addr, ProtocolMessage}
import org.slf4j.LoggerFactory

/**
  * The message handler for Addr message.
  */
object AddrMessageHandler {
  private lazy val logger = Logger( LoggerFactory.getLogger(AddrMessageHandler.getClass) )

  /** Handle Addr message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param addr The Addr message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, addr : Addr ) : Unit {
    // TODO : Implement
  }
}
