package io.scalechain.blockchain.net

import io.netty.channel.{Channel, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.ssl.SslHandler
import io.netty.util.concurrent.{Future, GenericFutureListener}
import io.scalechain.blockchain.proto.ProtocolMessage
import org.slf4j.LoggerFactory

/**
  * Handles a client-side channel.
  */
class NodeClientHandler(peerSet : PeerSet) extends SimpleChannelInboundHandler[ProtocolMessage] {
  private val logger = LoggerFactory.getLogger(classOf[NodeClientHandler])

  var messageHandler : ProtocolMessageHandler = null

  override def channelRead0(context : ChannelHandlerContext, message : ProtocolMessage) : Unit = {
    if (messageHandler == null ) {
      val peer = peerSet.add(context.channel())
      messageHandler = new ProtocolMessageHandler(peer, new PeerCommunicator(peerSet))
    }

    // Process the received message, and send message to peers if necessary.
    messageHandler.handle(message)
  }

  override def exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) : Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
