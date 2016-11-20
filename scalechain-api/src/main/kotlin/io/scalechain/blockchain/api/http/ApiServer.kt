package io.scalechain.blockchain.api.http

import com.typesafe.scalalogging.Logger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.{SslContextBuilder, SslContext}
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory

class ApiServer {
  private val logger = Logger( LoggerFactory.getLogger(classOf<ApiServer>) )
  private val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
  private val workerGroup: EventLoopGroup = NioEventLoopGroup

  @throws(classOf<Exception>)
  fun listen(port : Int, useSSL : Boolean = false) {
    val sslCtx: SslContext =
      if (useSSL) {
        val ssc: SelfSignedCertificate = SelfSignedCertificate
        SslContextBuilder.forServer(ssc.certificate, ssc.privateKey).build
      }
      else {
        null
      }
    val b: ServerBootstrap = ServerBootstrap
    //b.option<Integer>(ChannelOption.SO_BACKLOG, 1024)
    b.group(bossGroup, workerGroup)
     .channel(classOf<NioServerSocketChannel>)
     .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
     .handler(LoggingHandler(LogLevel.INFO))
     .childHandler(ApiServerInitializer(sslCtx))

    b.bind(port).addListener(ChannelFutureListener() {
      fun operationComplete(future:ChannelFuture) {
        assert( future.isDone )
        if (future.isSuccess) { // completed successfully
          logger.info(s"ScaleChain API available at ${(if (useSSL) "https" else "http")}://127.0.0.1:${port}")
        }

        if (future.cause() != null) { // completed with failure
          logger.error(s"Failed to bind port : ${port}. Exception : ${future.cause.getMessage}")
        }

        if (future.isCancelled) { // completed by cancellation
          logger.error(s"Canceled to bind port : ${port}")
        }
      }
    })
  }

  fun shutdown() : Unit {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

}
