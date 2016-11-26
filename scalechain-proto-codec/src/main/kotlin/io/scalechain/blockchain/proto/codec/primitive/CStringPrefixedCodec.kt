package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.Transcodable
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

/** A codec that prefixes data with a null terminated string.
 * This codec is used for creating prefixed keys in the storage layer.
 *
 * @param codecT The codec that decodes and encodes data right after the null terminated prefix string.
 * @tparam T The type of the data right after the null terminated prefix string.
 */
class CStringPrefixedCodec<T>(private val codecT : Codec<T>) : Codec<CStringPrefixed<T>> {
    override fun transcode(io : CodecInputOutputStream, obj : CStringPrefixed<T>? ) : CStringPrefixed<T>? {
        val prefix = Codecs.CString.transcode(io, obj?.prefix)
        val data  = codecT.transcode(io, obj?.data)
        if (io.isInput) {
            return CStringPrefixed<T>(
                prefix!!,
                data!!
            )
        }
        return null
    }
}
