package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

/**
 * Created by kangmo on 26/11/2016.
 */

class PolymorphicCodec<typeT, valueT>(private val typeIndicatorCodec : Codec<typeT>, private val typeClassNameToTypeIndicatorMap: Map<String, typeT>, private val typeIndicatorToCodecMap: Map<typeT, Codec<valueT>>) : Codec<valueT>{

    override fun transcode(io: CodecInputOutputStream, obj: valueT?): valueT? {
        if (io.isInput) {
            val typeIndicator = typeIndicatorCodec.transcode(io, null)
            return typeIndicatorToCodecMap[typeIndicator]!!.transcode(io, null)
        } else {
            val className = (obj!! as Any).javaClass.simpleName!!
            val typeIndicator = typeClassNameToTypeIndicatorMap[className]!!
            typeIndicatorCodec.transcode(io, typeIndicator)
            typeIndicatorToCodecMap[typeIndicator]!!.transcode(io, obj!!)
            return null
        }
    }
}