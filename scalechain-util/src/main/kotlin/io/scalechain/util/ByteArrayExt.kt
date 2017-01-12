package io.scalechain.util

import io.netty.buffer.ByteBuf

/**
 * Created by kangmo on 26/11/2016.
 */

object ByteArrayExt {
    /**
     * Construct a byte array that has only one byte.
     * @param byte The byte value that the byte array will have.
     */
    @JvmStatic
    fun from( byte : Byte) : ByteArray = ByteArray(1, {byte})
}