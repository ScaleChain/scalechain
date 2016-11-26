package io.scalechain.util

import io.netty.buffer.ByteBuf

/**
 * Created by kangmo on 26/11/2016.
 */

object ByteArrayExt {
    fun from( byte : Byte) : ByteArray = ByteArray(1, {byte})
}