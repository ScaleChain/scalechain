package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

class ByteCodec() : Codec<Byte> {
    override fun transcode(io : CodecInputOutputStream, obj : Byte? ) : Byte? {
        if (io.isInput) {
            return io.byteBuf.readByte()
        } else {
            io.byteBuf.writeByte(obj!!.toInt())
            return null
        }
    }
}

