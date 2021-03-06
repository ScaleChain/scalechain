package io.scalechain.blockchain.net

import io.netty.channel.*
import io.netty.handler.ssl.SslHandler
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import io.scalechain.blockchain.net.message.VersionFactory
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.ExceptionUtil
import io.scalechain.util.StackUtil
import org.slf4j.LoggerFactory

/**
  * Handles a server-side channel.
  */
class NodeServerHandler(private val peerSet : PeerSet) : SimpleChannelInboundHandler<ProtocolMessage>() {
  private val logger = LoggerFactory.getLogger(NodeServerHandler::class.java)

  var messageHandler : ProtocolMessageHandler? = null


  override fun channelActive(ctx : ChannelHandlerContext) : Unit {
    // Once session is secured, send a greeting and register the channel to the global channel
    // list so the channel received the messages from others.
    ctx.pipeline().get(SslHandler::class.java).handshakeFuture().addListener(
      object : GenericFutureListener<Future<Channel>> {
        override fun operationComplete(future : Future<Channel>)  {
          val remoteAddress = ctx.channel().remoteAddress()
          logger.info("Connection accepted from ${remoteAddress}")
          /*
          ctx.writeAndFlush(
            "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n")
          ctx.writeAndFlush(
            "Your session is protected by " +
              ctx.pipeline().get(classOf<SslHandler>).engine().getSession().getCipherSuite() +
            " cipher suite.\n")
          */

          assert(messageHandler == null)

          val peer = peerSet.add(ctx.channel())
          messageHandler = ProtocolMessageHandler(peer, PeerCommunicator(peerSet))

          // Upon successful connection, send the version message.
          peer.send( VersionFactory.create() )

          ctx.channel().closeFuture().addListener(object : ChannelFutureListener {
            override fun operationComplete(future:ChannelFuture) {
              assert( future.isDone )

              peerSet.remove(remoteAddress)

              if (future.isSuccess) { // completed successfully
                logger.info("Connection closed. Remote address : ${remoteAddress}")
              }

              if (future.cause() != null) { // completed with failure
                val causeDescription = ExceptionUtil.describe( future.cause().cause )
                logger.warn("Failed to close connection. Remote address : ${remoteAddress}. Exception : ${future.cause().message}, Stack Trace : ${StackUtil.getStackTrace(future.cause())} ${causeDescription}")
              }

              if (future.isCancelled) { // completed by cancellation
                logger.warn("Canceled to close connection. Remote address : ${remoteAddress}")
              }
            }
          })

        }
      }
    )
  }

  override fun channelRead0(context : ChannelHandlerContext, message : ProtocolMessage) : Unit {
    assert(messageHandler != null)
    // Process the received message, and send message to peers if necessary.

    messageHandler!!.handle(message)

    /*
        // Close the connection if the client has sent 'bye'.
        if ("bye".equals(msg.toLowerCase())) {
          ctx.close()
        }
    */
  }

  override fun exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) {
    val causeDescription = ExceptionUtil.describe( cause.cause )
    logger.error("${cause}. Stack : ${StackUtil.getStackTrace(cause)} ${causeDescription}")
    // TODO : BUGBUG : Need to close connection when an exception is thrown?
    //    ctx.close()
  }
}
