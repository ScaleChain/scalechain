package io.scalechain.blockchain.net

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.scalechain.blockchain.proto.ProtocolMessage
import org.slf4j.LoggerFactory

/**
  * Handles a client-side channel.
  */
class NodeClientHandler extends SimpleChannelInboundHandler[ProtocolMessage] {
  private val logger = LoggerFactory.getLogger(classOf[NodeClientHandler])

  val messageHandler = new ProtocolMessageHandler()

  override def channelRead0(context : ChannelHandlerContext, message : ProtocolMessage) : Unit = {
    // Step 1 : Process the received message to get the response to send if any.
    val responseMessageOption = messageHandler.handle(message)

    // Step 2 : Send the response back to the channel if any.
    responseMessageOption.map { responseMessage =>
      context.channel().writeAndFlush(message)
    }

//    println(msg)
  }

  override def exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) : Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
