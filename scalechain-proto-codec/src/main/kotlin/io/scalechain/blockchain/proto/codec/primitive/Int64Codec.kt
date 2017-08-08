package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import java.math.BigInteger


class Int64Codec() : Codec<Long> {
    override fun transcode(io : CodecInputOutputStream, obj : Long? ) : Long? {
        if (io.isInput) {
            return io.byteBuf.readLong()
        } else {
            io.byteBuf.writeLong(obj!!)
            return null
        }
    }
}


class Int64LCodec() : Codec<Long> {
    override fun transcode(io : CodecInputOutputStream, obj : Long? ) : Long? {
        if (io.isInput) {
            return io.byteBuf.readLongLE()
        } else {
            io.byteBuf.writeLongLE(obj!!)
            io.byteBuf
            return null
        }
    }
}

/*
data class UInt64Wrapper(val value: Long) {
    //override fun toString = s"UInt64(${UInt64.longToBigInt(value).toString})"
    override fun toString() = "UInt64(${value}L)"

    companion object {
        fun longToBigInt(unsignedLong: Long): java.math.BigInteger {
            return (BigInteger.valueOf(unsignedLong ushr 1).shiftLeft(1)).
                add(BigInteger.valueOf(unsignedLong and 1))
        }

        fun bigIntToLong(n:java.math.BigInteger ): Long {
            val smallestBit = (n.and(BigInteger.valueOf(1))).toLong()
            return ((n.shiftRight(1)).toLong() shl 1) or smallestBit
        }
    }
}
*/

class UInt64LCodec() : Codec<BigInteger> {
    private val Int64L = Int64LCodec()
    override fun transcode(io : CodecInputOutputStream, obj : BigInteger? ) : BigInteger? {
        if (io.isInput) {
            val longValue = Int64L.transcode(io, null)
            return longToBigInt(longValue!!)
        } else {
            Int64L.transcode(io, bigIntToLong(obj!!))
            return null
        }
    }

    companion object {
        private fun longToBigInt(unsignedLong: Long): java.math.BigInteger {
            return (BigInteger.valueOf(unsignedLong ushr 1).shiftLeft(1)).
                add(BigInteger.valueOf(unsignedLong and 1))
        }

        private fun bigIntToLong(n:java.math.BigInteger ): Long {
            val smallestBit = (n.and(BigInteger.valueOf(1))).toLong()
            return ((n.shiftRight(1)).toLong() shl 1) or smallestBit
        }
    }
}