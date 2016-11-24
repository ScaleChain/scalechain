package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

class ProvideCodec<T>(val objectSample : T) : Codec<T> {
    override fun transcode(io : CodecInputOutputStream, obj : T? ) : T? {
        if (io.isInput) {
            return objectSample
        } else {
            return null
        }
    }
}

