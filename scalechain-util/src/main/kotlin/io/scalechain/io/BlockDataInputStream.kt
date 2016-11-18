package io.scalechain.io

import java.io.*

import io.scalechain.util.Utils

/**
 * Created by kangmo on 11/2/15.
 */
class BlockDataInputStream(private val stream : InputStream) : DataInputStream(stream) {
    /** Read an integer that is written as little endian in the stream.
     *
     * @return The Int value we read.
     */
    fun readLittleEndianInt() : Int {
        return Integer.reverseBytes(readInt())
    }

    /** Read a long value that is written as little endian in the stream.
     *
     * @return The Long value we read.
     */
    fun readLittleEndianLong(): Long {
        return java.lang.Long.reverseBytes(readLong())
    }

    /*
      Original code from Mike Hearn's BitcoinJ.
      core/src/main/java/org/bitcoinj/core/VarInt.java
      public VarInt(byte[] buf, int offset) {

        int first = 0xFF & buf[offset];
        if (first < 253) {
          value = first;
          originallyEncodedSize = 1; // 1 data byte (8 bits)
        } else if (first == 253) {
          value = (0xFF & buf[offset + 1]) | ((0xFF & buf[offset + 2]) << 8);
          originallyEncodedSize = 3; // 1 marker + 2 data bytes (16 bits)
        } else if (first == 254) {
          value = Utils.readUint32(buf, offset + 1);
          originallyEncodedSize = 5; // 1 marker + 4 data bytes (32 bits)
        } else {
          value = Utils.readInt64(buf, offset + 1);
          originallyEncodedSize = 9; // 1 marker + 8 data bytes (64 bits)
        }
      }
  */


    fun readVarInt(): Long {
        val first = 0xFF and stream.read()

        if (first < 253) {
            // VarInt encoded in 1 byte. 1 data byte (8 bits)
            return first.toLong()
        } else if (first == 253) {
            // VarInt encoded in 3 bytes. 1 marker + 2 data bytes (16 bits)
            return ((0xFF and stream.read()) or ((0xFF and stream.read()) shl 8 )).toLong()
        } else if (first == 254) {
            // VarInt encoded in 5 bytes. 1 marker + 4 data bytes (32 bits)
            return Utils.readUint32(stream)
        } else {
            // VarInt encoded in 9 bytes. 1 marker + 8 data bytes (64 bits)
            return Utils.readInt64(stream)
        }
    }

    /** Read N bytes and return the byte array.
     *
     * @param size The number of bytes are going to read.
     * @return The byte array we read.
     */
    // BUGBUG : Interface Change, Array[Byte] -> ByteArray
    fun readBytes(size : Int): ByteArray {
        val bytes = ByteArray(size)
        read(bytes)
        return bytes
    }

    // BUGBUG : Interface Change, Array[Byte] -> ByteArray
    fun readBytes(bytes : ByteArray)  {
        read(bytes)
    }
}
