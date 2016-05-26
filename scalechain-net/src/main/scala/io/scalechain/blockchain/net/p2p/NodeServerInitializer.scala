package io.scalechain.blockchain.net

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SslContext
import io.scalechain.blockchain.net.p2p.{BitcoinProtocolEncoder, BitcoinProtocolDecoder}

/**
  * Creates a newly configured {@link ChannelPipeline} for a new channel.
  */
class NodeServerInitializer(sslCtx : SslContext, peerSet : PeerSet) extends ChannelInitializer[SocketChannel] {

  override def initChannel(ch : SocketChannel) : Unit = {
    val pipeline : ChannelPipeline = ch.pipeline()

    // Add SSL handler first to encrypt and decrypt everything.
    // In this example, we use a bogus certificate in the server side
    // and accept any invalid certificates in the client side.
    // You will need something more complicated to identify both
    // and server in the real world.
    pipeline.addLast(sslCtx.newHandler(ch.alloc()))

    // On top of the SSL handler, add the text line codec.
    pipeline.addLast(new BitcoinProtocolDecoder())
    pipeline.addLast(new BitcoinProtocolEncoder())

    // and then business logic.
    pipeline.addLast(new NodeServerHandler(peerSet))
  }
}
