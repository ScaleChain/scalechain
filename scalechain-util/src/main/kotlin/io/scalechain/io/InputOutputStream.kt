package io.scalechain.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled


/** A stream that has either input or output stream.
 * If it has an input stream, it reads data from the stream and returns the read value.
 * If it has an output stream, it write the argument data to the stream and returns the argument unchanged.
 *
 * TODO : Pattern matching an Either for each method could be costly. Optimize the code.
 *
 * @param stream Either an input stream or an output stream.
 */
open class InputOutputStream( open val byteBuf : ByteBuf, open val isInput : Boolean ) {
    fun fixedBytes(length : Int, bytes : ByteBuf?) : ByteBuf {
        assert(length >= 0)

        return if (isInput) {
            byteBuf.readBytes(length)
        } else {
            assert(bytes != null)
            byteBuf.writeBytes(bytes!!, length)
        }
    }
}
