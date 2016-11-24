package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

class OptionalCodec<T>(val flagCodec : Codec<Boolean> = Codecs.Boolean, val valueCodec : Codec<T> ) : Codec<T?> {
    override fun transcode(io : CodecInputOutputStream, obj : (T?)? ) : (T?)? {
        if (io.isInput) {
            val hasValue = flagCodec.transcode(io, null)!!
            val objectOption : T? =
                if (hasValue) {
                    valueCodec.transcode(io, null)
                } else {
                    null
                }
            val objectOptionOption : (T?)? = objectOption
            return objectOptionOption
        } else {
            val objectOption : T? = obj!!
            if ( objectOption == null) {
                flagCodec.transcode(io, false)
            } else {
                flagCodec.transcode(io, true)
                valueCodec.transcode(io, objectOption!!)
            }
            return null
        }
    }
}
