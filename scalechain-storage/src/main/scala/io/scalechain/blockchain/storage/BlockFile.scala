package io.scalechain.blockchain.storage

import java.io.{RandomAccessFile, File}
import java.nio.ByteBuffer

import io.scalechain.blockchain.{ErrorCode, BlockStorageException}
import io.scalechain.blockchain.proto.codec.NetworkProtocol
import io.scalechain.blockchain.proto.{ProtocolMessage, Block}
import io.scalechain.blockchain.storage.RecordFile.RecordLocator
import scodec.bits.BitVector

class BlockAccessFile(path : File, maxFileSize : Long) {
  val file = new RandomAccessFile(path, "rw")
  val fileChannel = file.getChannel


  def offset() : Long = fileChannel.position()

  def moveTo(offset : Long) : Unit = {
    if ( fileChannel.position() != offset ) {
      fileChannel.position(offset)
    }
  }

  def read(offset : Long, size : Int) : ByteBuffer = {
    moveTo(offset)

    val buffer = ByteBuffer.allocate(size)
    fileChannel.read(buffer)
    buffer
  }

  def write(offset : Long, buffer : ByteBuffer) = {
    moveTo(offset)

    fileChannel.write(buffer)
  }

  def append(buffer:ByteBuffer) : Unit = {
    fileChannel.write(buffer)
  }


  def flush() : Unit = {
    fileChannel.force(true)
  }

}


object RecordFile {
  case class RecordLocator(offset : Long, size : Int)
}

/** A record file that contains set of records.
  *
  * For reading/writing a file, we use random access file to get the file channel, and read/write from the channel.
  *
  * Details :
  * http://tutorials.jenkov.com/java-nio/file-channel.html
  */
class RecordFile[T <: ProtocolMessage](path : File, maxFileSize : Long, messageType : String)(implicit protocol : NetworkProtocol ) extends BlockAccessFile(path, maxFileSize){

  def readRecord(locator : RecordLocator) : T = {
    val buffer = read(locator.offset, locator.size)
    val bitVector = BitVector.view(buffer)
    protocol.decode(messageType, bitVector).asInstanceOf[T]
  }

  def appendRecord(record : T) : RecordLocator = {
    val bitVector = protocol.encode(record)
    val initialOffset = offset()
    val buffer = bitVector.toByteBuffer
    if (initialOffset + buffer.capacity() > maxFileSize) {
      throw new BlockStorageException(ErrorCode.OutOfFileSpace)
    }
    append(buffer)
    RecordLocator(initialOffset, buffer.capacity())
  }
}



