package io.scalechain.blockchain.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{ChannelFuture, EventLoopGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate

class NodeServer(peerSet : PeerSet) {
  protected[net] val bossGroup : EventLoopGroup = new NioEventLoopGroup(1)
  protected[net] val workerGroup : EventLoopGroup = new NioEventLoopGroup()

  def listen(port : Int) : ChannelFuture = {
    // TODO : BUGBUG : SelfSignedCertificate is insecure. Replace it with another one.
    val ssc : SelfSignedCertificate = new SelfSignedCertificate()
    val sslCtx : SslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
      .build()

    val b : ServerBootstrap = new ServerBootstrap()

    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new NodeServerInitializer(sslCtx, peerSet))

    //b.bind(port).sync().channel().closeFuture().sync()
    b.bind(port)
  }

  def shutdown() : Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}