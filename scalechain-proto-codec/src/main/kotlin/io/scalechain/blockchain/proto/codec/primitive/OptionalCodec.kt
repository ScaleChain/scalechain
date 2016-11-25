package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.util.Option
import io.scalechain.util.Option.Some
import io.scalechain.util.Option.None

class OptionalCodec<T>(val flagCodec : Codec<Boolean> = Codecs.Boolean, val valueCodec : Codec<T> ) : Codec<Option<T>> {
    override fun transcode(io : CodecInputOutputStream, obj : Option<T>? ) : Option<T>? {
        if (io.isInput) {
            val hasValue = flagCodec.transcode(io, null)!!
            if (hasValue) {
                return Some(valueCodec.transcode(io, null)!!)
            } else {
                return None()
            }
        } else {
            val optionObject = obj!!
            when(optionObject) {
                is None -> flagCodec.transcode(io, false)
                is Some -> {
                    flagCodec.transcode(io, true)
                    valueCodec.transcode(io, optionObject.value)
                }
            }
            return null
        }
    }
}
