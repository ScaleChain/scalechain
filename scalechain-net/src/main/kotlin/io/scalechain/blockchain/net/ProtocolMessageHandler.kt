package io.scalechain.blockchain.net

import io.scalechain.blockchain.net.handler.*
import io.scalechain.blockchain.proto.*
import org.slf4j.LoggerFactory

/** Handle protocol messages after getting connected to a peer.
  * The protocol message handler has the message handing context, which has the connected peer, and peer communicator that can communicate with all connected peers.
  *
  * @param peer The peer that this node is handler is communicating.
  * @param communicator The peer communicator that can communicate with any of peers connected to this node.
  */
class ProtocolMessageHandler(peer : Peer, communicator : PeerCommunicator)  {
  private val logger = LoggerFactory.getLogger(ProtocolMessageHandler::class.java)

  val context = MessageHandlerContext(peer, communicator)

  /** Handle a message coming from the TCP stream.
    *
    * @param message The messages to handle.
    * @return The list of responses we created after handling each message in messages.
    */
  fun handle(message : ProtocolMessage): Unit {
    when {
      message is Version -> {
        VersionMessageHandler.handle(context, message)
      }
      message is Ping -> {
        PingMessageHandler.handle(context, message)
      }
      message is Pong -> {
        PongMessageHandler.handle(context, message)
      }
      message is Verack -> {
        VerackMessageHandler.handle(context, message)
      }
      message is Addr -> {
        AddrMessageHandler.handle(context, message)
      }
      message is Inv -> {
        InvMessageHandler.handle(context, message)
      }
      message is Headers -> {
        HeadersMessageHandler.handle(context, message)
      }
      message is GetData -> {
        GetDataMessageHandler.handle(context, message)
      }
      message is GetBlocks -> {
        GetBlocksMessageHandler.handle(context, message)
      }
      message is GetHeaders -> {
        GetHeadersMessageHandler.handle(context, message)
      }
      message is Transaction -> {
        TxMessageHandler.handle(context, message)
      }
      message is Block -> {
        BlockMessageHandler.handle(context, message)
      }
      else -> {
        logger.warn("Received a message, but done nothing : ${message.javaClass.name}" )
      }
    }
  }
}
