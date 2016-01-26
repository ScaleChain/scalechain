package io.scalechain.io

/** A stream that has either input or output stream.
  * If it has an input stream, it reads data from the stream and returns the read value.
  * If it has an output stream, it write the argument data to the stream and returns the argument unchanged.
  *
  * TODO : Pattern matching an Either for each method could be costly. Optimize the code.
  *
  * @param stream Either an input stream or an output stream.
  */
class InputOutputStream( stream : Either[BlockDataInputStream, BlockDataOutputStream] ) {

  def littleEndianInt(value : Int ) : Int = {
    stream match {
      case Right(outputStream) => {
        outputStream.writeLittleEndianInt(value)
        value
      }
      case Left(inputStream) => {
        inputStream.readLittleEndianInt()
      }
    }
  }

  def littleEndianLong(value : Long) : Long = {
    stream match {
      case Right(outputStream) => {
        outputStream.writeLittleEndianLong(value)
        value
      }
      case Left(inputStream) => {
        inputStream.readLittleEndianLong()
      }
    }
  }

  def variableInt(value : Long) : Long = {
    stream match {
      case Right(outputStream) => {
        outputStream.writeVarInt(value)
        value
      }
      case Left(inputStream) => {
        inputStream.readVarInt()
      }
    }
  }

  def bytes(bytes : Array[Byte]) : Unit = {
    stream match {
      case Right(outputStream) => {
        outputStream.writeBytes(bytes)
      }
      case Left(inputStream) => {
        inputStream.readBytes(bytes)
      }
    }
  }
}
