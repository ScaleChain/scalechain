package io.scalechain.blockchain.storage.record

import java.io.{File, RandomAccessFile}
import java.nio.ByteBuffer

/**
  * Created by kangmo on 3/12/16.
  */
class BlockAccessFile(path : File, maxFileSize : Long) {
  val file = new RandomAccessFile(path, "rw")
  val fileChannel = file.getChannel

  def size()   : Long = fileChannel.size()

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

  def close() = {
    fileChannel.close()
    file.close()
  }
}
