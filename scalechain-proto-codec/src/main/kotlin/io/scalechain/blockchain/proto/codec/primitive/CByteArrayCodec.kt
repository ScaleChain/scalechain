package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

class CByteArrayCodec() : Codec<ByteArray> {
    override fun transcode(io : CodecInputOutputStream, obj : ByteArray? ) : ByteArray? {
        if (io.isInput) {
            val bytes = arrayListOf<Byte>()
            var b : Byte
            do {
                b = io.byteBuf.readByte()
            } while( b != 0.toByte() )

            return bytes.toByteArray()
        } else {
            io.byteBuf.writeBytes(obj!!)
            io.byteBuf.writeByte(0)

            return null
        }
    }
}
