package io.scalechain.blockchain.api.http

import io.netty.channel.{ChannelInitializer, ChannelPipeline}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpResponseEncoder, HttpRequestDecoder, HttpServerCodec}
import io.netty.handler.ssl.SslContext

class ApiServerInitializer(sslCtx: SslContext)  extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel) {
    val p: ChannelPipeline = ch.pipeline
    if (sslCtx != null) {
      p.addLast(sslCtx.newHandler(ch.alloc))
    }
//    p.addLast(new HttpServerCodec)
    p.addLast(new HttpRequestDecoder)
    // Uncomment the following line if you don't want to handle HttpChunks.
    //p.addLast(new HttpObjectAggregator(1048576));
    p.addLast(new HttpResponseEncoder)
    // Remove the following line if you don't want automatic content compression.
    //p.addLast(new HttpContentCompressor());
    p.addLast(new ApiServerHandler)
  }
}
