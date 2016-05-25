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


object SecureChatClient {
  val HOST = System.getProperty("host", "127.0.0.1")
  val PORT = Integer.parseInt(System.getProperty("port", "8992"))

  def main(args:Array[String]) = {
    new SecureChatClient().connect(HOST, PORT)
  }
}
/**
  * Simple SSL chat client.
  */
class SecureChatClient {
  def connect(address : String, port : Int) : Unit = {
    // Configure SSL.
    val sslCtx : SslContext = SslContextBuilder.forClient()
      // TODO : BUGBUG : Do not use an insecure trust manager.
      // From the comment of InsecureTrustManagerFactory :
      //   An insecure that trusts all X.509 certificates without any verification.
      .trustManager(InsecureTrustManagerFactory.INSTANCE).build()

    val group : EventLoopGroup = new NioEventLoopGroup()
    try {
      val b : Bootstrap = new Bootstrap()
         b.group(group)
        .channel(classOf[NioSocketChannel])
        .handler(new SecureChatClientInitializer(sslCtx, address, port))

      // Start the connection attempt.
      val ch : Channel = b.connect(address, port).sync().channel()

      // Read commands from the stdin.
      var lastWriteFuture : ChannelFuture = null
      val in : BufferedReader = new BufferedReader(new InputStreamReader(System.in))

      var finished = false
      while (!finished) {
        val line : String = in.readLine()
        if (line == null) {
          finished = true
        } else {
          // Sends the received line to the server.
          lastWriteFuture = ch.writeAndFlush(line + "\r\n")

          // If user typed the 'bye' command, wait until the server closes
          // the connection.
          if ("bye".equals(line.toLowerCase())) {
            ch.closeFuture().sync()
            finished = true
          }
        }
      }

      // Wait until all messages are flushed before closing the channel.
      if (lastWriteFuture != null) {
        lastWriteFuture.sync()
      }
    } finally {
      // The connection is closed automatically on shutdown.
      group.shutdownGracefully()
    }
  }
}
