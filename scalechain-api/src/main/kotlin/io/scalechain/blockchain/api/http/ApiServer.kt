package io.scalechain.blockchain.api.http

import com.typesafe.scalalogging.Logger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory

class ApiServer {
  private val logger = LoggerFactory.getLogger(ApiServer::class.java)

  private val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
  private val workerGroup: EventLoopGroup = NioEventLoopGroup()

  fun listen(port : Int, useSSL : Boolean = false) {
    val sslCtx: SslContext? =
      if (useSSL) {
        val ssc: SelfSignedCertificate = SelfSignedCertificate()
        SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build()
      }
      else {
        null
      }
    val b: ServerBootstrap = ServerBootstrap()
    //b.option<Integer>(ChannelOption.SO_BACKLOG, 1024)
    b.group(bossGroup, workerGroup)
     .channel(NioServerSocketChannel::class.java)
     .option(ChannelOption.SO_KEEPALIVE, true)
     .handler(LoggingHandler(LogLevel.INFO))
     .childHandler(ApiServerInitializer(sslCtx))

    b.bind(port).addListener(ChannelFutureListener() {
      fun operationComplete(future:ChannelFuture) {
        assert( future.isDone )
        if (future.isSuccess) { // completed successfully
          logger.info("ScaleChain API available at ${(if (useSSL) "https" else "http")}://127.0.0.1:${port}")
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
