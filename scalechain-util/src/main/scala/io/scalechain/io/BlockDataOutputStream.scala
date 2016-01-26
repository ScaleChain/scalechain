package io.scalechain.io

import java.io.{DataOutputStream, OutputStream}

import io.scalechain.util.Utils

/**
 * Created by kangmo on 11/15/15.
 */
class BlockDataOutputStream(stream: OutputStream) extends DataOutputStream(stream) {
  /** Write an integer as little endian format on the stream.
    * @param value The int value to write.
    */
  def writeLittleEndianInt(value: Int): Unit = {
    writeInt(Integer.reverseBytes(value))
  }

  /** Write a long value as little endian on the stream.
    * @param value The long value to write.
    */
  def writeLittleEndianLong(value: Long): Unit = {
    writeLong(java.lang.Long.reverseBytes(value))
  }


  /**
    Original code from Mike Hearn's BitcoinJ.
    core/src/main/java/org/bitcoinj/core/VarInt.java

    public byte[] encode() {
        byte[] bytes;
        switch (sizeOf(value)) {
            case 1:
                return new byte[]{(byte) value};
            case 3:
                return new byte[]{(byte) 253, (byte) (value), (byte) (value >> 8)};
            case 5:
                bytes = new byte[5];
                bytes[0] = (byte) 254;
                Utils.uint32ToByteArrayLE(value, bytes, 1);
                return bytes;
            default:
                bytes = new byte[9];
                bytes[0] = (byte) 255;
                Utils.uint64ToByteArrayLE(value, bytes, 1);
                return bytes;
    }


  */

  /** Write a long value as a variable integer format on the stream.
    * @param value The long value to write.
    */
  def writeVarInt(value : Long): Unit = {
    Utils.sizeOf(value) match {
      case 1 => {
        writeByte((value & 0xFF).toByte)
      }
      case 3 => {
        writeByte(253)
        writeByte((value & 0xFF).toByte)
        writeByte(((value>>8) & 0xFF).toByte)
      }
      case 5 => {
        writeByte(254)
        Utils.uint32ToByteStreamLE(value, this)
      }
      case _ => {
        writeByte(255)
        Utils.int64ToByteStreamLE(value, this)
      }
    }
  }

  /** Write a byte array.
    * @param bytes The byte array to write.
    */
  def writeBytes(bytes : Array[Byte]) : Unit = {
    write(bytes)
  }

}
