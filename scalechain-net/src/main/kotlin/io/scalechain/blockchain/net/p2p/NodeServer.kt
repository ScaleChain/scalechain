package io.scalechain.blockchain.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory

class NodeServer(private val peerSet : PeerSet) {
  private val logger = LoggerFactory.getLogger(NodeServer::class.java)

  protected val bossGroup : EventLoopGroup = NioEventLoopGroup(1)
  protected val workerGroup : EventLoopGroup = NioEventLoopGroup()

  fun listen(port : Int) : ChannelFuture {
    // TODO : BUGBUG : SelfSignedCertificate is insecure. Replace it with another one.
    val ssc : SelfSignedCertificate = SelfSignedCertificate()
    val sslCtx : SslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
      .build()

    val b : ServerBootstrap = ServerBootstrap()

    b.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel::class.java)
      .option(ChannelOption.SO_KEEPALIVE, true)
      .handler(LoggingHandler(LogLevel.INFO))
      .childHandler(NodeServerInitializer(sslCtx, peerSet))

    //b.bind(port).sync().channel().closeFuture().sync()
    return b.bind(port).addListener(object : ChannelFutureListener {
      override fun operationComplete(future:ChannelFuture) {
        assert( future.isDone )
        if (future.isSuccess) { // completed successfully
          logger.info("Successfully bound port : ${port}")
        }

        if (future.cause() != null) { // completed with failure
          logger.error("Failed to bind port : ${port}. Exception : ${future.cause().message}")
        }

        if (future.isCancelled) { // completed by cancellation
          logger.error("Canceled to bind port : ${port}")
        }
      }
    })
  }

  fun shutdown() : Unit {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}