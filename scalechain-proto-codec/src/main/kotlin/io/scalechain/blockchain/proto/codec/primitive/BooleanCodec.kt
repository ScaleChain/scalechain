package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

class BooleanCodec() : Codec<Boolean> {
  override fun transcode(io : CodecInputOutputStream, obj : Boolean? ) : Boolean? {
    if (io.isInput) {
      return (io.byteBuf.readByte() != 0.toByte())
    } else {
      io.byteBuf.writeByte( if (obj!!) 1 else 0 )
      return null
    }
  }
}
