package io.scalechain.blockchain

import java.io.{InputStream, DataInputStream}

/**
 * Created by kangmo on 11/2/15.
 */
class BlockDataInputStream(stream : InputStream) extends DataInputStream(stream) {
  /** Read an integer that is written as little endian in the stream.
    *
    * @return The Int value we read.
    */
  def readLittleEndianInt(): Int = {
    Integer.reverseBytes(readInt());
  }

  /** Read a long value that is written as little endian in the stream.
    *
    * @return The Long value we read.
    */
  def readLittleEndianLong(): Long = {
    java.lang.Long.reverseBytes(readLong());
  }

  def readVarInt(): Int = {
    // BUGBUG : Need to make sure if it is signed or unsigned.
    // Also need to make sure that the varint format in block files have the same format.
    Varint.readUnsignedVarInt(this)
  }

  /** Read N bytes and return the byte array.
   *
   * @param size The number of bytes are going to read.
   * @return The byte array we read.
   */
  def readBytes(size : Int):Array[Byte] = {
    val bytes = new Array[Byte](size);
    read(bytes)
    bytes
  }
}
