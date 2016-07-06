package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.{PrivateVersion, Verack, Version}
import io.scalechain.util.Config
import org.slf4j.LoggerFactory

object PrivateVersionMessageHandler {
  private lazy val logger = Logger( LoggerFactory.getLogger(PrivateVersionMessageHandler.getClass) )

  /** Handle PrivateVersion message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param privateVersion The PrivateVersion message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, privateVersion : PrivateVersion ) : Unit = {
    logger.info(s"Version accepted : ${privateVersion}")
    if (Config.isPrivate) {
      context.peer.updatePrivateVersion(privateVersion)
    }
  }
}
