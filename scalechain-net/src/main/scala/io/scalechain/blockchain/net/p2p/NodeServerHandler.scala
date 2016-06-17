package io.scalechain.blockchain.net

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.ssl.SslHandler
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import io.netty.util.concurrent.GlobalEventExecutor
import io.scalechain.blockchain.proto.ProtocolMessage
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

import java.net.InetAddress

object NodeServerHandler {
  // TODO : Investiate why we need the channel group.
  val channels : ChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}

/**
  * Handles a server-side channel.
  */
class NodeServerHandler(peerSet : PeerSet) extends SimpleChannelInboundHandler[ProtocolMessage] {
  private val logger = LoggerFactory.getLogger(classOf[NodeServerHandler])

  import NodeServerHandler._

  var messageHandler : ProtocolMessageHandler = null


  override def channelActive(ctx : ChannelHandlerContext) : Unit = {
    // Once session is secured, send a greeting and register the channel to the global channel
    // list so the channel received the messages from others.
    ctx.pipeline().get(classOf[SslHandler]).handshakeFuture().addListener(
      new GenericFutureListener[Future[Channel]]() {
        override def operationComplete(future : Future[Channel])  {
          logger.info(s"Connection accepted from ${ctx.channel().remoteAddress()}")
          /*
          ctx.writeAndFlush(
            "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n")
          ctx.writeAndFlush(
            "Your session is protected by " +
              ctx.pipeline().get(classOf[SslHandler]).engine().getSession().getCipherSuite() +
            " cipher suite.\n")
          */
          channels.add(ctx.channel())
        }
      })
  }

  override def channelRead0(context : ChannelHandlerContext, message : ProtocolMessage) : Unit = {
    if (messageHandler == null ) {
      val peer = peerSet.add(context.channel())
      messageHandler = new ProtocolMessageHandler(peer, new PeerCommunicator(peerSet))
    }

    // Process the received message, and send message to peers if necessary.
    messageHandler.handle(message)

/*
    // Close the connection if the client has sent 'bye'.
    if ("bye".equals(msg.toLowerCase())) {
      ctx.close()
    }
*/
  }

  override def exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) {
    cause.printStackTrace()
    ctx.close()
  }
}
