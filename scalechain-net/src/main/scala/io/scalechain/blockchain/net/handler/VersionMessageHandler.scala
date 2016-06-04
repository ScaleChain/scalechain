package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.proto.{Verack, ProtocolMessage, Version}
import org.slf4j.LoggerFactory

/**
  * The message handler for Version message.
  */
object VersionMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(VersionMessageHandler.getClass)

  /** Handle Version message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param version The Version message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, version : Version ) : Option[ProtocolMessage] = {
    logger.info(s"Version accepted : ${version}")
    // TODO : Implement - Update peerInfo.version.
    Some(Verack())
  }
}
