package io.scalechain.blockchain.net

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

import java.io.BufferedReader
import java.io.InputStreamReader


object NodeClient {
  val HOST = System.getProperty("host", "127.0.0.1")
  val PORT = Integer.parseInt(System.getProperty("port", "8992"))

  def main(args:Array[String]) : Unit = {
    new NodeClient().connect(HOST, PORT)
  }
}
/**
  * Simple SSL chat client.
  */
class NodeClient extends AutoCloseable {
  protected[net] val group : EventLoopGroup = new NioEventLoopGroup()

  def connect(address : String, port : Int) : ChannelFuture = {
    // Configure SSL.
    val sslCtx : SslContext = SslContextBuilder.forClient()
      // TODO : BUGBUG : Do not use an insecure trust manager.
      // From the comment of InsecureTrustManagerFactory :
      //   An insecure that trusts all X.509 certificates without any verification.
      .trustManager(InsecureTrustManagerFactory.INSTANCE).build()

    val b : Bootstrap = new Bootstrap()
       b.group(group)
      .channel(classOf[NioSocketChannel])
      .handler(new NodeClientInitializer(sslCtx, address, port))

    // Start the connection attempt.
    //val channel : Channel = b.connect(address, port).sync().channel()
    val channelFuture : ChannelFuture = b.connect(address, port)
    channelFuture
  }

  def close() : Unit = {
    group.shutdownGracefully()
  }
}
