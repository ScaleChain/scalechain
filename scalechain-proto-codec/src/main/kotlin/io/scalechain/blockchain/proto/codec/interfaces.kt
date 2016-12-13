package io.scalechain.blockchain.proto.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.primitive.ProvideCodec
import io.scalechain.io.InputOutputStream
import io.scalechain.util.toByteArray
import kotlin.reflect.KClass

/**
 * InputOutputStream that can transcode an object based on a codec
 */
class CodecInputOutputStream(override val byteBuf : ByteBuf, override val isInput : Boolean) : InputOutputStream(byteBuf, isInput) {
    /**
     * Transcode an object based on a codec
     * @param codec The codec that knows how to encode/decode the object
     * @param value The object to transcode.
     */
    fun<T> transcode(codec : Codec<T>, value : T?) : T? {
        return codec.transcode(this, value)
    }
}

/**
 * Encodes and decodes a transcodable object.
 */
interface Codec<T> {
    /**
     * Implements both encoding and decoding object.
     * InputOutputObject holds either BlockDataInputStream or BlockDataOutputStream.
     * If it holds BlockDataInputStream, it decodes the transcodable from the input stream.
     * If it holds BlockDataOutputStream, it encodes the transcodable into the output stream.
     *
     * Why? If we have separate encode/decode function for an object,
     * we will have redundant code for each field we encode and decode in the two functions.
     */
    fun transcode(io : CodecInputOutputStream, obj : T? ) : T?

    fun decode(data : ByteArray) : T? = decode(Unpooled.wrappedBuffer(data))

    fun decode(byteBuf : ByteBuf) : T? = transcode(CodecInputOutputStream(byteBuf, isInput = true), null)

    fun encode(value: T) : ByteArray = encodeAsByteBuf(value).toByteArray()

    fun encodeAsByteBuf(value:T) : ByteBuf {
        val byteBuf = Unpooled.buffer()
        transcode( CodecInputOutputStream(byteBuf, isInput = false), value)
        return byteBuf
    }
}


interface ProtocolMessageCodec<T> : Codec<T> {
    abstract val command : String
    abstract val clazz : Class<T>
}

