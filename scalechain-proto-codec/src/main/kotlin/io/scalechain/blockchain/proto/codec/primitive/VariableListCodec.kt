package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.io.InputOutputStream

/**
 * Variable length transcoder that encodes/decodes a uniform type.
 * VariableTranscodableCodec transcodes a list of uniform type.
 *
 * @param lengthCodec The codec for transcoding the length in front of actual encoded objects.
 * @param valueCodec The codec for the actual object.
 */
class VariableListCodec<T>(val lengthCodec : Codec<Long>, val valueCodec : Codec<T> ) : Codec<List<T>>{
    override fun transcode(io : CodecInputOutputStream, value : List<T>? ) : List<T>? {
        val valueLength : Long? = value?.size?.toLong()
        val length : Long? = io.transcode(lengthCodec, valueLength)
        if (io.isInput) {
            val mutableList = mutableListOf<T>()
            for (i in 1..length!!) {
                val v = io.transcode(valueCodec, null)
                mutableList.add(v!!)
            }
            return mutableList
        } else {
            assert(value != null)
            assert(valueLength!! <= Int.MAX_VALUE)
            for (i in 0 until valueLength!!.toInt()) {
                io.transcode(valueCodec, value?.get(i))
            }
            return null
        }
    }
}
