package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import java.nio.charset.Charset


class NullTerminatedStringCodec(val charset : Charset) : Codec<String> {
    override fun transcode(io : CodecInputOutputStream, obj : String? ) : String? {
        if (io.isInput) {
            val bytes = mutableListOf<Byte>()
            var b : Byte
            do {
                b = io.byteBuf.readByte()
            } while( b != 0.toByte() )

            return kotlin.text.String(bytes.toByteArray(), charset)
        } else {
            val byteArray = obj!!.toByteArray(charset)

            io.byteBuf.writeBytes(byteArray)
            io.byteBuf.writeByte(0)

            return null
        }
    }
}

