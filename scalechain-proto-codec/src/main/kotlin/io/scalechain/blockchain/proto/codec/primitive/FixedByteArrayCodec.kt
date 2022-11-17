package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

import io.netty.buffer.ByteBufUtil
import io.scalechain.util.ByteBufExt
import io.scalechain.util.toByteArray

class FixedByteArrayCodec(val length : Int) : Codec<ByteArray> {
    override fun transcode(io : CodecInputOutputStream, obj : ByteArray? ) : ByteArray? {
        if (io.isInput) {
            val byteBuf = io.fixedBytes(length, null)
            val byteArray = byteBuf.toByteArray()
            if (byteArray.isNotEmpty()) {
                val fullyReleased = byteBuf.release()
                assert(fullyReleased)
                assert(byteBuf.refCnt() == 0)
            }
            return byteArray
        }
        val byteBuf = ByteBufExt.from(obj!!)
        io.fixedBytes(length, byteBuf)
        return null
    }
}