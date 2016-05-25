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
import scala.collection.JavaConverters._

import java.net.InetAddress

object SecureChatServerHandler {
  val channels : ChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}

/**
  * Handles a server-side channel.
  */
class SecureChatServerHandler extends SimpleChannelInboundHandler[String] {
  import SecureChatServerHandler._

  override def channelActive(ctx : ChannelHandlerContext) : Unit = {
    // Once session is secured, send a greeting and register the channel to the global channel
    // list so the channel received the messages from others.
    ctx.pipeline().get(classOf[SslHandler]).handshakeFuture().addListener(
      new GenericFutureListener[Future[Channel]]() {
        override def operationComplete(future : Future[Channel])  {
          ctx.writeAndFlush(
            "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
          ctx.writeAndFlush(
            "Your session is protected by " +
              ctx.pipeline().get(classOf[SslHandler]).engine().getSession().getCipherSuite() +
            " cipher suite.\n");

          channels.add(ctx.channel());
        }
      })
  }

  override def channelRead0(ctx : ChannelHandlerContext, msg : String) : Unit = {
    // Send the received message to all channels but the current one.
    for ( c <- channels.asScala) {
      if (c != ctx.channel()) {
        c.writeAndFlush("[" + ctx.channel().remoteAddress() + "] " + msg + '\n')
      } else {
        c.writeAndFlush("[you] " + msg + '\n')
      }
    }

    // Close the connection if the client has sent 'bye'.
    if ("bye".equals(msg.toLowerCase())) {
      ctx.close()
    }
  }

  override def exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) {
    cause.printStackTrace()
    ctx.close()
  }
}
