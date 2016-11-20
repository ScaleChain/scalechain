package io.scalechain.blockchain.net

import com.typesafe.scalalogging.Logger
import io.netty.channel.{Channel, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.ssl.SslHandler
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.{Future, GenericFutureListener}
import io.scalechain.blockchain.net.p2p.NodeThrottle
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.{StackUtil, ExceptionUtil}
import org.slf4j.LoggerFactory

/**
  * Handles a client-side channel.
  */
class NodeClientHandler(peerSet : PeerSet) extends SimpleChannelInboundHandler[ProtocolMessage] {
  private val logger = Logger( LoggerFactory.getLogger(classOf[NodeClientHandler]) )

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
    val causeDescription = ExceptionUtil.describe( cause.getCause )
    logger.error(s"${cause}. Stack : ${StackUtil.getStackTrace(cause)} ${causeDescription}")
    // TODO : BUGBUG : Need to close connection when an exception is thrown?
    //    ctx.close()
  }
}
