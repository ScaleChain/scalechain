package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

/*
enum class RejectType {
  REJECT_MALFORMED, REJECT_INVALID, REJECT_OBSOLETE, REJECT_DUPLICATE, REJECT_NONSTANDARD, REJECT_DUST, REJECT_INSUFFICIENTFEE, REJECT_CHECKPOINT
}
*/

/**
 * Mapped enumeration codec. Transcodes an enum based on value mappings.
 *
 * @param valueCodec The codec for transcoding the mapped value.
 * @param enumMap A map from an enum value to a serialized value
 */
class MappedEnumCodec<valueT, enumT>(val valueCodec: Codec<valueT>, val enumMap: Map<enumT, valueT>) : Codec<enumT> {
    private val enumValuePairList = enumMap.entries.map { entry ->
        Pair(entry.value, entry.key)
    }
    // A map from serialized value to an enum instance
    private val valueMap = mapOf( * enumValuePairList.toTypedArray() )

    override fun transcode(io : CodecInputOutputStream, obj : enumT? ) : enumT? {
        if (io.isInput) {
            val value = valueCodec.transcode(io, null)
            return valueMap[value]
        } else {
            val value = enumMap[obj!!]
            valueCodec.transcode(io, value)
            return null
        }
    }
}

