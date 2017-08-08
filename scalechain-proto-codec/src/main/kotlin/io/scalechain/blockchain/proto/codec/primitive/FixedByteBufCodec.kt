package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.io.InputOutputStream

class FixedByteBufCodec(val length : Int) : Codec<ByteBuf> {
    override fun transcode(io : CodecInputOutputStream, obj : ByteBuf? ) : ByteBuf? {
        return io.fixedBytes(length, obj)
    }
}