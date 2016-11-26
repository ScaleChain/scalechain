package io.scalechain.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * Created by kangmo on 21/11/2016.
 */

object ByteBufExt {
    fun from( hexString : String ) : ByteBuf {
        val bytes = HexUtil.bytes(hexString)
        return Unpooled.wrappedBuffer(bytes)
    }
    fun from( bytes : ByteArray ) : ByteBuf {
        return Unpooled.wrappedBuffer(bytes)
    }
}

internal object Limits {
    val UINT_MAX   = 4294967295L
    val USHORT_MAX = 65535
}

fun ByteBuf.writeUnsignedIntLE(value : Long) {
    assert(value <= Limits.UINT_MAX)
    assert(value >= 0L)

    this.writeByte((0xFFL and value).toInt())
    this.writeByte((0xFFL and (value shr 8)).toInt())
    this.writeByte((0xFFL and (value shr 16)).toInt())
    this.writeByte((0xFFL and (value shr 24)).toInt())
}

fun ByteBuf.writeUnsignedInt(value : Long) {
    assert(value <= Limits.UINT_MAX)
    assert(value >= 0L)

    this.writeByte((0xFFL and (value shr 24)).toInt())
    this.writeByte((0xFFL and (value shr 16)).toInt())
    this.writeByte((0xFFL and (value shr 8)).toInt())
    this.writeByte((0xFFL and value).toInt())
}

fun ByteBuf.writeUnsignedShortLE(value : Int) {
    assert(value <= Limits.USHORT_MAX)
    assert(value >= 0)

    this.writeByte((0xFF and value))
    this.writeByte((0xFF and (value shr 8)))
}

fun ByteBuf.writeUnsignedShort(value : Int) {
    assert(value <= Limits.USHORT_MAX)
    assert(value >= 0)

    this.writeByte((0xFF and (value shr 8)))
    this.writeByte((0xFF and value))
}

fun ByteBuf.kotlinHex() : String {
    // BUGBUG : Optimize by passing ByteBuf to HexUtil not to copy the byte array
    return HexUtil.kotlinHex(this.array())
}