package io.scalechain.blockchain

import java.io.{InputStream, DataInputStream}
import collection.mutable.HashMap

import io.scalechain.blockchain.util.Utils

class FieldStatsCalculator {
  val fieldMap = new HashMap[String, Long]()
  def fieldSize(fieldName: String, length : Int) : Unit = {
    val lengthOption = fieldMap.get(fieldName)
    if (lengthOption.isDefined) {
      fieldMap.put(fieldName, lengthOption.get + length.toLong )
    } else {
      fieldMap.put(fieldName, length.toLong)
    }
  }

  override def toString() = {
    fieldMap.toString()
  }
}

object BlockDataInputStream {
  val stats = new FieldStatsCalculator();
}


/**
 * Created by kangmo on 11/2/15.
 */
class BlockDataInputStream(stream : InputStream) extends DataInputStream(stream) {

  /** Read an integer that is written as little endian in the stream.
    *
    * @return The Int value we read.
    */
  def readLittleEndianInt(fieldName : String): Int = {
    BlockDataInputStream.stats.fieldSize(fieldName, 4)
    Integer.reverseBytes(readInt());
  }

  /** Read a long value that is written as little endian in the stream.
    *
    * @return The Long value we read.
    */
  def readLittleEndianLong(fieldName : String): Long = {
    BlockDataInputStream.stats.fieldSize(fieldName, 8)
    java.lang.Long.reverseBytes(readLong());
  }

  /*
    Original code from Mike Hearn's BitcoinJ.
    core/src/main/java/org/bitcoinj/core/Utils.java

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
*/


  def readVarInt(fieldName : String): Long = {
    val first = 0xFF & stream.read()
    if (first < 253) {
      // VarInt encoded in 1 byte. 1 data byte (8 bits)
      BlockDataInputStream.stats.fieldSize(fieldName, 1)
      first
    } else if (first == 253) {
      BlockDataInputStream.stats.fieldSize(fieldName, 3)
      // VarInt encoded in 3 bytes. 1 marker + 2 data bytes (16 bits)
      (0xFF & stream.read()) | ((0xFF & stream.read()) << 8 )
    } else if (first == 254) {
      BlockDataInputStream.stats.fieldSize(fieldName, 5)
      // VarInt encoded in 5 bytes. 1 marker + 4 data bytes (32 bits)
      Utils.readUint32(stream)
    } else {
      BlockDataInputStream.stats.fieldSize(fieldName, 9)
      // VarInt encoded in 9 bytes. 1 marker + 8 data bytes (64 bits)
      Utils.readInt64(stream)
    }
  }

  /** Read N bytes and return the byte array.
   *
   * @param size The number of bytes are going to read.
   * @return The byte array we read.
   */
  def readBytes(size : Int, fieldName : String):Array[Byte] = {
    val bytes = new Array[Byte](size);

    BlockDataInputStream.stats.fieldSize(fieldName, size)

    read(bytes)
    bytes
  }
}
