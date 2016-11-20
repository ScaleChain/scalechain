package io.scalechain.io

/** A stream that has either input or output stream.
 * If it has an input stream, it reads data from the stream and returns the read value.
 * If it has an output stream, it write the argument data to the stream and returns the argument unchanged.
 *
 * TODO : Pattern matching an Either for each method could be costly. Optimize the code.
 *
 * @param stream Either an input stream or an output stream.
 */
// BUGBUG : Changed interface. From Either<BlockDataInputStream, BlockDataOutputStream> to istream, ostream
class InputOutputStream( val istream : BlockDataInputStream?, val ostream : BlockDataOutputStream? ) {

    fun littleEndianInt(value : Int ) : Int {
        if (istream != null) {
            return istream.readLittleEndianInt()
        }
        if (ostream != null) {
            ostream.writeLittleEndianInt(value)
            return value
        }
        assert(false)
        return 0
    }

    fun littleEndianLong(value : Long ) : Long {
        if (istream != null) {
            return istream.readLittleEndianLong()
        }
        if (ostream != null) {
            ostream.writeLittleEndianLong(value)
            return value
        }
        assert(false)
        return 0L
    }

    fun variableInt(value : Long) : Long {
        if (istream != null) {
            return istream.readVarInt()
        }
        if (ostream != null) {
            ostream.writeVarInt(value)
            return value
        }
        assert(false)
        return 0L

    }

    // BUGBUG : Interface change from Array<Byte> to ByteArray
    fun bytes(bytes : ByteArray) {

        if (istream != null) {
            return istream.readBytes(bytes)
        }
        if (ostream != null) {
            ostream.writeBytes(bytes)
        }
    }

    fun close() {
        if (istream != null) {
            istream.close()
        }

        if (ostream != null) {
            ostream.close()
        }
    }

    fun flush() {
        if (istream != null) {
            // Unable to flush istream.
            assert(false)
        }

        if (ostream != null) {
            ostream.flush()
        }
    }

}
