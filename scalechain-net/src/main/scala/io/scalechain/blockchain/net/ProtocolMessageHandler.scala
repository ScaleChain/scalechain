package io.scalechain.blockchain.net

import io.scalechain.blockchain.net.handler._
import io.scalechain.blockchain.proto._
import org.slf4j.LoggerFactory

/** Handle protocol messages after getting connected to a peer.
  * The protocol message handler has the message handing context, which has the connected peer, and peer communicator that can communicate with all connected peers.
  *
  * @param peer The peer that this node is handler is communicating.
  * @param communicator The peer communicator that can communicate with any of peers connected to this node.
  */
class ProtocolMessageHandler(peer : Peer, communicator : PeerCommunicator)  {
  val context = new MessageHandlerContext(peer, communicator)
  private val logger = LoggerFactory.getLogger(classOf[ProtocolMessageHandler])

  /** Handle a message coming from the TCP stream.
    *
    * @param message The messages to handle.
    * @return The list of responses we created after handling each message in messages.
    */
  def handle(message : ProtocolMessage): Option[ProtocolMessage] = {
    // Return Some[ProtocolMessage] if we need to reply a message. Return None otherwise.
    message match {
      case version: Version => {
        VersionMessageHandler.handle(context, version)
      }
      case ping : Ping => {
        PingMessageHandler.handle(context, ping)
      }
      case pong : Pong => {
        PongMessageHandler.handle(context, pong)
      }
      case verack: Verack => {
        VerackMessageHandler.handle(context, verack)
      }
      case addr: Addr => {
        AddrMessageHandler.handle(context, addr)
      }
      case inv: Inv => {
        InvMessageHandler.handle(context, inv)
      }
      case headers: Headers => {
        HeadersMessageHandler.handle(context, headers)
      }
      case getHeaders: GetHeaders => {
        GetHeadersMessageHandler.handle(context, getHeaders)
      }
      case transaction: Transaction => {
        TransactionMessageHandler.handle(context, transaction)
      }
      case block: Block => {
        BlockMessageHandler.handle(context, block)
      }
      case m: ProtocolMessage => {
        logger.warn("Received a message, but done nothing : " + m.getClass.getName)
        None
      }
    }
  }
}
