package io.scalechain.blockchain.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate

/**
  * Simple SSL chat server.
  */
object SecureChatServer {
  val PORT = Integer.parseInt(System.getProperty("port", "8992"))

  def main(args : Array[String]) = {
    new SecureChatServer().listen(PORT)
  }
}

class SecureChatServer() {
  def listen(port : Int) = {
    val ssc : SelfSignedCertificate = new SelfSignedCertificate()
    val sslCtx : SslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
      .build()

    val bossGroup : EventLoopGroup = new NioEventLoopGroup(1)
    val workerGroup : EventLoopGroup = new NioEventLoopGroup()
    try {
      val b : ServerBootstrap = new ServerBootstrap()

      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new SecureChatServerInitializer(sslCtx))

      b.bind(port).sync().channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}