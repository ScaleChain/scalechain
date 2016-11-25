package io.scalechain.io

import java.io.*

import io.scalechain.util.Utils

/*
/**
 * Created by kangmo on 11/15/15.
 */
class BlockDataOutputStream(private val stream: OutputStream) : DataOutputStream(stream) {
    /** Write an integer as little endian format on the stream.
     * @param value The int value to write.
     */
    fun writeLittleEndianInt(value: Int) {
        writeInt(Integer.reverseBytes(value))
    }

    /** Write a long value as little endian on the stream.
     * @param value The long value to write.
     */
    fun writeLittleEndianLong(value: Long) {
        writeLong(java.lang.Long.reverseBytes(value))
    }


    /**
    Original code from Mike Hearn's BitcoinJ.
    core/src/main/java/org/bitcoinj/core/VarInt.java

    public byte<> encode() {
    byte<> bytes;
    switch (sizeOf(value)) {
    case 1:
    return byte<>{(byte) value};
    case 3:
    return byte<>{(byte) 253, (byte) (value), (byte) (value >> 8)};
    case 5:
    bytes = byte<5>;
    bytes<0> = (byte) 254;
    Utils.uint32ToByteArrayLE(value, bytes, 1);
    return bytes;
    default:
    bytes = byte<9>;
    bytes<0> = (byte) 255;
    Utils.uint64ToByteArrayLE(value, bytes, 1);
    return bytes;
    }


     */


    /** Write a byte array.
     * @param bytes The byte array to write.
     */
    // BUGBUG : Interface Change, ByteArray -> ByteArray
    fun writeBytes(bytes : ByteArray)  {
        write(bytes)
    }

}
*/