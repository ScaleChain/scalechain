package io.scalechain.blockchain.cli

import java.io._

import io.scalechain.blockchain.block.Block
import io.scalechain.blockchain.block.codec.BlockParser
import io.scalechain.io.BlockDataInputStream


/** For each block read by the block reader, we will have a function call.
 *
 */
trait BlockReadListener {
  /** This function is called whenever we finish reading and decoding a block.
   *
   * @param block The block read by the Reader.
   */
  def onBlock(block : Block ): Unit
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
  def readFully(blockFile : File): Unit = {
    var stream : BlockDataInputStream= null
    try {
      stream = new BlockDataInputStream( new DataInputStream( new BufferedInputStream (new FileInputStream(blockFile))) )
      while( readBlock(stream) ) {
        // do nothing
      }
    } finally {
      stream.close()
    }
  }

  /**
   * Read a block from the input stream.
   * @param stream The byte array stream where we read the block data.
   * @return True if a block was read, False if we met EOF of the input block file.
   */
  def readBlock(stream : BlockDataInputStream): Boolean = {
    val parser = new BlockParser(stream)
    val blockOption = parser.parse()
    if (blockOption.isDefined) {
      blockListener.onBlock(blockOption.get)
      true
    } else {
      false
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
  def readFrom(path : String) {
    val directory = new File(path)
    // For each file in the path
    for (file <- directory.listFiles.sortBy(_.getName())
         if (file.getName().startsWith("blk") && file.getName().endsWith(".dat")) ) {
      val fileReader = new  BlockFileReader(blockListener)
      fileReader.readFully(file)
    }
  }
}
