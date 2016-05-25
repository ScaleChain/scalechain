package io.scalechain.blockchain.net

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
/**
  * Handles a client-side channel.
  */
class SecureChatClientHandler extends SimpleChannelInboundHandler[String] {

  override def channelRead0(ctx : ChannelHandlerContext, msg : String) : Unit = {
    println(msg)
  }

  override def exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) : Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
