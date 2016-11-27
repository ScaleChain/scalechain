package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.Verack
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.Version
import org.slf4j.LoggerFactory

/**
  * The message handler for Version message.
  */
object VersionMessageHandler {
  private val logger = LoggerFactory.getLogger(VersionMessageHandler.javaClass)

  /** Handle Version message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param version The Version message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, version : Version ) : Unit {
    logger.info(s"Version accepted : ${version}")
    context.peer.updateVersion(version)
    context.peer.send(Verack())
  }
}
