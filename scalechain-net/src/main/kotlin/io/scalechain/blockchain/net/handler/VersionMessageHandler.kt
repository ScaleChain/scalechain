package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.net.Node
import io.scalechain.blockchain.net.controller.ChainedHandlers
import io.scalechain.blockchain.net.controller.InitialBlockDownloadController
import io.scalechain.blockchain.net.controller.MessageHandler
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Verack
import io.scalechain.blockchain.proto.Version
import org.slf4j.LoggerFactory

/**
  * The message handler for Version message.
  */
object VersionMessageHandler : MessageHandler<Version> {
  private val logger = LoggerFactory.getLogger(VersionMessageHandler.javaClass)

  /** Handle Version message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param version The Version message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  override fun handle( context : MessageHandlerContext, version : Version ) : Boolean {
    logger.info("Version accepted : ${version}")
    context.peer.updateVersion(version)
    context.peer.send(Verack())
    Node.get().updateStatus()

    return false
  }
}
