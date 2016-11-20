package io.scalechain.blockchain.storage.record

import java.io.{File, RandomAccessFile}
import java.nio.ByteBuffer

/**
  * Created by kangmo on 3/12/16.
  */
class BlockAccessFile(path : File, maxFileSize : Long) {
  val file = RandomAccessFile(path, "rw")
  val fileChannel = file.getChannel

  fun size()   : Long = fileChannel.size()

  fun offset() : Long = fileChannel.position()

  protected<storage> fun moveTo(offset : Long) : Unit {
    if ( fileChannel.position() != offset ) {
      fileChannel.position(offset)
    }
  }

  fun read(offset : Long, size : Int) : ByteBuffer {
    moveTo(offset)

    val buffer = ByteBuffer.allocate(size)
    fileChannel.read(buffer)
    buffer
  }
/*
  fun write(offset : Long, buffer : ByteBuffer) {
    moveTo(offset)

    fileChannel.write(buffer)
  }
*/
  fun append(buffer:ByteBuffer) : Unit {
    // If we are not at the end of the file, move to the end of it.
    if (offset() != size()) {
      moveTo(size())
    }

    fileChannel.write(buffer)
  }

  fun flush() : Unit {
    fileChannel.force(true)
  }

  fun close() {
    fileChannel.force(true)
    fileChannel.close()
    file.close()
  }
}
