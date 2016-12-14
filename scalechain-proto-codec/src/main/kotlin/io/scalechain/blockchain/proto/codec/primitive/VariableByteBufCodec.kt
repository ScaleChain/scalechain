package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

/**
 * Variable length transcoder that encodes/decodes a uniform type.
 * VariableTranscodableCodec transcodes a list of uniform type.
 *
 * @param lengthCodec The codec for transcoding the length in front of actual encoded objects.
 * @param valueCodec The codec for the actual object.
 */
class VariableByteBufCodec(val lengthCodec : Codec<Long>) : Codec<ByteBuf>{
  override fun transcode(io : CodecInputOutputStream, obj : ByteBuf? ) : ByteBuf? {
    val valueLength = obj?.readableBytes()
    val length : Long? = io.transcode(lengthCodec, valueLength?.toLong())
    if (io.isInput) {
      assert(length!! <= Int.MAX_VALUE )
      return io.fixedBytes(length!!.toInt(), null)
    } else {
      assert(valueLength!! <= Int.MAX_VALUE )
      io.fixedBytes(valueLength!!.toInt(), obj)
      return null
    }
  }
}
