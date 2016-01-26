package io.scalechain.blockchain.proto.codec

import java.io.{InputStream, OutputStream}

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.{VersionMessage, ProtocolMessage}
import io.scalechain.io.{BlockDataInputStream, BlockDataOutputStream}

class BitcoinProtocolCodec {
  def encode(message : ProtocolMessage, out : OutputStream): Unit = {
    val stream = new BlockDataOutputStream(out)
    val envelope = BitcoinMessageEnvelope.build(message)
    BitcoinMessageEnvelope.serialize(envelope, stream)
  }
  def decode(in : InputStream) : ProtocolMessage = {
    val stream = new BlockDataInputStream(in)
    val envelope = BitcoinMessageEnvelope.parse(stream)
    BitcoinMessageEnvelope.verify(envelope)
    envelope.payload
  }
}


