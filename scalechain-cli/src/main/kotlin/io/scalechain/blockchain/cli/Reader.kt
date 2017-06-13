package io.scalechain.blockchain.cli

import io.netty.buffer.Unpooled
import java.io.*
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.FatalException
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


/** For each block read by the block reader, we will have a function call.
 *
 */
interface BlockReadListener {
  /** This function is called whenever we finish reading and decoding a block.
   *
   * @param block The block read by the Reader.
   */
  fun onBlock(block : Block ): Unit
}


/** Read a block file such as blk00000.dat and produce list of blocks
 *
  * @param blockListener We will call onBlock function of the listener for each block we read.
 */
class BlockFileReader(val blockListener : BlockReadListener) {
  /** Read all blocks in the file. Call onBlock for each block we read.
   *
    * @param blockFile the file to read.
   */
  fun readFully(blockFile : File): Unit {
    val raf = RandomAccessFile(blockFile, "r")
    raf.use { raf ->
      val channel = raf.getChannel()
      val byteBuffer : MappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
      byteBuffer.load()
      val wrappedBuffer = Unpooled.wrappedBuffer(byteBuffer)
      val stream = CodecInputOutputStream(wrappedBuffer, isInput = true)
      while( readBlock(stream) ) ;
    }
  }

  /** Parse the byte array stream to get a block.
    *
    * This function reads the following fields.
    * Magic no : value always 0xD9B4BEF9, 4 bytes
    * Blocksize : number of bytes following up to end of block, 4 bytes
    * Blockheader : consists of 6 items, 80 bytes
    * Transaction counter : positive integer VI = VarInt, 1 - 9 bytes
    * transactions : the (non empty) list of transactions, <Transaction counter>-many transactions
    *
    * Source : https://en.bitcoin.it/wiki/Block
    *
    * @return Some(Block) if we successfully read a block. None otherwise.
    */
  fun parseBlock(stream : CodecInputOutputStream) : Block? {
    try {
      val magic = Codecs.UInt32L.transcode(stream, null)

      // BUGBUG : Does this work even though the integer we read is a signed int?
      if (magic != 0xD9B4BEF9)
        throw FatalException(ErrorCode.InvalidBlockMagic)

      Codecs.UInt32L.transcode(stream, null)!!
      return BlockCodec.transcode(stream, null)
    } catch(e : IndexOutOfBoundsException) {
      return null
    }
  }


  /**
   * Read a block from the input stream.
   * @param stream The byte array stream where we read the block data.
   * @return True if a block was read, False if we met EOF of the input block file.
   */
  fun readBlock(stream : CodecInputOutputStream): Boolean {
    val blockOption = parseBlock(stream)
    if (blockOption != null) {
      blockListener.onBlock(blockOption)
      return true
    } else {
      return false
    }
  }
}


/** Read the blockchain data downloaded by the reference Bitcoin core implementation.
 *
 */
class BlockDirectoryReader(val blockListener : BlockReadListener) {

  /** Reads list of blocks written in Blockchain.
    * This function starts reading blk00000.dat and produced blocks.
    * After finishing reading the file, it continues to read blk00001.dat, and so on.
    *
    * @param path The path that has blkNNNNN.dat files.
    * @return
    */
  fun readFrom(path : String) : Boolean {
    val directory = File(path)
    if (directory.exists()) {
      // For each file in the path
      for (file in directory.listFiles().sortedBy { file : File -> file.getName() } ) {
        if (file.getName().startsWith("blk") && file.getName().endsWith(".dat")) {
          val fileReader = BlockFileReader(blockListener)
          fileReader.readFully(file)
        }
      }
      return true
    } else {
      return false
    }
  }
}
