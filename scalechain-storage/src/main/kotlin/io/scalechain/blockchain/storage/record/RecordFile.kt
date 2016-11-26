package io.scalechain.blockchain.storage.record

import java.io.File
import java.nio.ByteBuffer

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.BlockStorageException
import io.scalechain.blockchain.ErrorCode


/** A record file that contains set of records.
  *
  * For reading/writing a file, we use random access file to get the file channel, and read/write from the channel.
  *
  * Details :
  * http://tutorials.jenkov.com/java-nio/file-channel.html
  */
class RecordFile(private val path : File, private val maxFileSize : Long) : BlockAccessFile(path, maxFileSize){
  init {
    // Move to the end of file so that we can append records at the end of the file.
    moveTo( size() )
  }

  fun<T> readRecord(codec : Codec<T>, locator : RecordLocator) : T {
    val rawRecord = read(locator.offset, locator.size)
    return codec.decode(rawRecord)!!
  }

  fun<T> appendRecord(codec : Codec<T>, record : T) : RecordLocator {
    // Move to the end of the file if we are not.
    if ( offset() <= size() ) {
      moveTo(size())
    }

    val serializedBytes = codec.encode(record)
    val initialOffset = offset()
    val buffer = ByteBuffer.wrap(serializedBytes)
    if (initialOffset + buffer.capacity() > maxFileSize) {
      throw BlockStorageException(ErrorCode.OutOfFileSpace)
    }
    append(buffer)
    return RecordLocator(initialOffset, buffer.capacity())
  }
}



