package io.scalechain.blockchain.proto.codec.primitive
import io.scalechain.blockchain.proto.codec.Codec

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.io.InputOutputStream

import io.scalechain.util.writeUnsignedIntLE

object VariableInt {
    fun decode(byteBuf: ByteBuf): Long {
        fun nextByte() = byteBuf.readByte().toInt()

        val first = 0xFF and nextByte()

        if (first < 253) {
            // VarInt encoded in 1 byte. 1 data byte (8 bits)
            return first.toLong()
        } else if (first == 253) {
            // VarInt encoded in 3 bytes. 1 marker + 2 data bytes (16 bits)
            return ((0xFF and nextByte()) or ((0xFF and nextByte()) shl 8 )).toLong()
        } else if (first == 254) {
            // VarInt encoded in 5 bytes. 1 marker + 4 data bytes (32 bits)
            return byteBuf.readUnsignedIntLE()
        } else {
            // VarInt encoded in 9 bytes. 1 marker + 8 data bytes (64 bits)
            return byteBuf.readLongLE()
        }
    }

    private fun sizeOf(value: Long): Int {
        // if negative, it's actually a very large unsigned long value
        if (value < 0) return 9 // 1 marker + 8 data bytes
        if (value < 253) return 1 // 1 data byte
        if (value <= 0xFFFFL) return 3 // 1 marker + 2 data bytes
        if (value <= 0xFFFFFFFFL) return 5 // 1 marker + 4 data bytes
        return 9 // 1 marker + 8 data bytes
    }

    /** Write a long value as a variable integer format on the stream.
     * @param value The long value to write.
     */
    fun encode(byteBuf : ByteBuf, value : Long) {
        when (sizeOf(value)) {
            1 -> byteBuf.writeByte((value and 0xFF).toByte().toInt())
            3 -> {
                byteBuf.writeByte(253)
                byteBuf.writeByte((value and 0xFF).toByte().toInt())
                byteBuf.writeByte(((value shr 8) and 0xFF).toByte().toInt())
            }
            5 -> {
                byteBuf.writeByte(254)
                byteBuf.writeUnsignedIntLE(value) // extention function
            }
            else -> {
                byteBuf.writeByte(255)
                byteBuf.writeLongLE(value)
            }
        }
    }
}

class VariableIntCodec : Codec<Long> {
    override fun transcode(io : CodecInputOutputStream, obj : Long? ) : Long? {
        if (io.isInput) {
            return VariableInt.decode(io.byteBuf)
        } else {
            VariableInt.encode(io.byteBuf, obj!!)
            return null
        }
    }
}
