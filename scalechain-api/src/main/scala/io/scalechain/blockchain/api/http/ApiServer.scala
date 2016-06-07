package io.scalechain.blockchain.api.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, ChannelOption, EventLoopGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.{SslContextBuilder, SslContext}
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory

class ApiServer {
  private val logger = LoggerFactory.getLogger(classOf[ApiServer])
  private val bossGroup: EventLoopGroup = new NioEventLoopGroup(1)
  private val workerGroup: EventLoopGroup = new NioEventLoopGroup

  @throws(classOf[Exception])
  def listen(port : Int, useSSL : Boolean = false) {
    val sslCtx: SslContext =
      if (useSSL) {
        val ssc: SelfSignedCertificate = new SelfSignedCertificate
        SslContextBuilder.forServer(ssc.certificate, ssc.privateKey).build
      }
      else {
        null
      }
    val b: ServerBootstrap = new ServerBootstrap
    //b.option[Integer](ChannelOption.SO_BACKLOG, 1024)
    b.group(bossGroup, workerGroup)
     .channel(classOf[NioServerSocketChannel])
     .handler(new LoggingHandler(LogLevel.INFO))
     .childHandler(new ApiServerInitializer(sslCtx))

    b.bind(port)
    logger.info("ScaleChain API available at " + (if (useSSL) "https" else "http") + "://127.0.0.1:" + port + '/')
  }

  def shutdown() : Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

}
