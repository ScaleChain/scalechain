package io.scalechain.blockchain.api.http

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.ssl.SslContext

class ApiServerInitializer(private val sslCtx: SslContext?)  : ChannelInitializer<SocketChannel>() {

  override fun initChannel(ch: SocketChannel) {
    val p: ChannelPipeline = ch.pipeline()
    if (sslCtx != null) {
      p.addLast(sslCtx.newHandler(ch.alloc()))
    }
//    p.addLast(HttpServerCodec)
    p.addLast(HttpRequestDecoder())
    // Uncomment the following line if you don't want to handle HttpChunks.
    //p.addLast(HttpObjectAggregator(1048576));
    p.addLast(HttpResponseEncoder())
    // Remove the following line if you don't want automatic content compression.
    //p.addLast(HttpContentCompressor());
    p.addLast(ApiServerHandler())
  }
}
