package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import java.nio.charset.Charset


class CStringCodec(val charset : Charset) : Codec<String> {
    val Codec = Codecs.CByteArray
    override fun transcode(io : CodecInputOutputStream, obj : String? ) : String? {
        if (io.isInput) {
            val bytes = Codec.transcode(io, null)

            return kotlin.text.String(bytes!!, charset)
        } else {
            val byteArray = obj!!.toByteArray(charset)

            Codec.transcode(io, byteArray)

            return null
        }
    }
}

