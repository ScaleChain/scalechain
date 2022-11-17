package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.util.toByteArray

/**
 * Variable length transcoder that encodes/decodes a uniform type.
 * VariableTranscodableCodec transcodes a list of uniform type.
 *
 * @param lengthCodec The codec for transcoding the length in front of actual encoded objects.
 * @param valueCodec The codec for the actual object.
 */
class VariableByteArrayCodec(val lengthCodec : Codec<Long>) : Codec<ByteArray>{
    override fun transcode(io : CodecInputOutputStream, obj : ByteArray? ) : ByteArray? {
        val valueLength = obj?.size
        val length : Long? = io.transcode(lengthCodec, valueLength?.toLong())
        if (io.isInput) {
            assert(length!! <= Int.MAX_VALUE )
            val byteBuf = io.fixedBytes(length!!.toInt(), null)
            val byteArray = byteBuf.toByteArray()

            // Release only if the byteBuf is not empty.
            // If byte array is empty, release call does not decrease reference count.
            if (byteArray.isNotEmpty()) {
                val fullyReleased = byteBuf.release()
                assert(fullyReleased)
                assert(byteBuf.refCnt() == 0)
            }
            return byteArray
        } else {
            assert(valueLength!! <= Int.MAX_VALUE )
            io.fixedBytes(valueLength!!.toInt(), Unpooled.wrappedBuffer(obj!!))
            return null
        }
    }
}
