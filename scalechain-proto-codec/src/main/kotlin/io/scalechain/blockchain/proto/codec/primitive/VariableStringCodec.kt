package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.ByteBufAllocator
import java.nio.charset.Charset
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.netty.buffer.Unpooled
/**
 * Created by kangmo on 22/11/2016.
 */
class VariableStringCodec(val lengthCodec : Codec<Long>) : Codec<String> {
    val VariableByteBufCodec = VariableByteBufCodec(lengthCodec)

    override fun transcode(io : CodecInputOutputStream, obj : String? ) : String? {
        if (io.isInput) {
            val byteBuf : ByteBuf = VariableByteBufCodec.transcode(io, null)!!
            return byteBuf.toString(Utf8CharSet)
        } else {
            // BUGBUG : Unnecessary byte array copy happens from string to bytebuf?
            // Need to understand what happens when we write a ByteBuf into another ByteBuf.
            // If no byte array is copied during this process, it is ok.
            val byteArray = obj!!.toByteArray(Utf8CharSet)
            VariableByteBufCodec.transcode(io, Unpooled.wrappedBuffer(byteArray))
            return null
        }
    }

    companion object {
        val Utf8CharSet = Charset.forName("UTF-8")
    }
}
