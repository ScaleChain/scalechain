package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.util.toByteArray
import io.scalechain.util.ByteBufExt

class FixedReversedByteArrayCodec(val length : Int) : Codec<ByteArray> {
    override fun transcode(io : CodecInputOutputStream, obj : ByteArray? ) : ByteArray? {
        return FixedByteArrayCodec(length).transcode(io, obj?.reversedArray())?.reversedArray()
    }
}