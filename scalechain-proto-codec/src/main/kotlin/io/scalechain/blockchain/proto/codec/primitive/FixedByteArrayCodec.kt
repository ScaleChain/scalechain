package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.io.InputOutputStream
import io.scalechain.util.ByteBufExt

class FixedByteArrayCodec(val length : Int) : Codec<ByteArray> {
    override fun transcode(io : CodecInputOutputStream, obj : ByteArray? ) : ByteArray? {
        if (io.isInput) {
            val byteBuf = io.fixedBytes(length, null)
            return byteBuf.array()
        }
        val byteBuf = ByteBufExt.from(obj!!)
        io.fixedBytes(length, byteBuf)
        return null
    }
}