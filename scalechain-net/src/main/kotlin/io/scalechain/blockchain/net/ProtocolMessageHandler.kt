package io.scalechain.blockchain.net

import io.scalechain.blockchain.net.controller.ChainedHandlers
import io.scalechain.blockchain.net.controller.InitialBlockDownloadController
import io.scalechain.blockchain.net.controller.MessageHandler
import io.scalechain.blockchain.net.handler.*
import io.scalechain.blockchain.proto.*
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/** Handle protocol messages after getting connected to a peer.
  * The protocol message handler has the message handing context, which has the connected peer, and peer communicator that can communicate with all connected peers.
  *
  * @param peer The peer that this node is handler is communicating.
  * @param communicator The peer communicator that can communicate with any of peers connected to this node.
  */
class ProtocolMessageHandler(peer : Peer, communicator : PeerCommunicator)  {
  private val logger = LoggerFactory.getLogger(ProtocolMessageHandler::class.java)

  val context = MessageHandlerContext(peer, communicator)

  fun<MessageType> mapping(javaClassName : String, vararg handlers: MessageHandler<MessageType>) : Pair<String, MessageHandler<ProtocolMessage>> {
    return javaClassName to ChainedHandlers<MessageType>(
      *handlers
    ) as ChainedHandlers<ProtocolMessage>
  }

  val handlerMap = mapOf<String, MessageHandler<ProtocolMessage>>(
    mapping(
      Version::class.java.simpleName,
      VersionMessageHandler, // pass through
      // IBD checks for a new connection from a peer to decide whether to initiate IBD
      // For example, IBD may start after receiving Version message from at least five peers
      // to select the best peer which is the best block among the five peers.
      InitialBlockDownloadController.versionMessageHandler
    ),
    mapping(
      Verack::class.java.simpleName,
      VerackMessageHandler
    ),
    mapping(
      Ping::class.java.simpleName,
      PingMessageHandler
    ),
    mapping(
      Pong::class.java.simpleName,
      PongMessageHandler
    ),
    mapping(
      Addr::class.java.simpleName,
      AddrMessageHandler
    ),
    mapping(
      Inv::class.java.simpleName,
      InitialBlockDownloadController.invMessageHandler, // During IBD, this handler processes inv message.
      InvMessageHandler
    ),
    mapping(
      Headers::class.java.simpleName,
      HeadersMessageHandler
    ),
    mapping(
      GetData::class.java.simpleName,
      GetDataMessageHandler
    ),
    mapping(
      GetBlocks::class.java.simpleName,
      GetBlocksMessageHandler
    ),
    mapping(
      GetHeaders::class.java.simpleName,
      GetHeadersMessageHandler
    ),
    mapping(
      Transaction::class.java.simpleName,
      InitialBlockDownloadController.transactionMessageHandler, // Ignore Tx message if IBD is in progress
      TxMessageHandler
    ),
    mapping(
      Block::class.java.simpleName,
      InitialBlockDownloadController.blockMessageHandler, // During IBD, this handler processes block message.
      BlockMessageHandler
    )
  )

  /** Handle a message coming from the TCP stream.
    *
    * @param message The messages to handle.
    * @return The list of responses we created after handling each message in messages.
    */
  fun handle(message : ProtocolMessage): Unit {
    val handler = handlerMap.get(message.javaClass.simpleName)
    if (handler == null) {
      logger.warn("Received a message, but no handler was registered for the message class : ${message.javaClass.name}" )
    } else {
      handler.handle(context, message)
    }
  }
}
