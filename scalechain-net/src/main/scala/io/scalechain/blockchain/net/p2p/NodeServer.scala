package io.scalechain.blockchain.net

import com.typesafe.scalalogging.Logger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{ChannelOption, ChannelFutureListener, ChannelFuture, EventLoopGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.scalechain.util.{ExceptionUtil, StackUtil}
import org.slf4j.LoggerFactory

class NodeServer(peerSet : PeerSet) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[NodeClient]) )

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
      .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
      .handler(new LoggingHandler(LogLevel.TRACE))
      .childHandler(new NodeServerInitializer(sslCtx, peerSet))

    //b.bind(port).sync().channel().closeFuture().sync()
    b.bind(port).addListener(new ChannelFutureListener() {
      def operationComplete(future:ChannelFuture) {
        assert( future.isDone )
        if (future.isSuccess) { // completed successfully
          logger.info(s"Successfully bound port : ${port}")
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

  def shutdown() : Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}