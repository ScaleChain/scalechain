package io.scalechain.blockchain

/**
 * Created by kangmo on 11/3/15.
 */
object DumpChain {
  /** Dump all blocks in blkNNNNN.dat files in a directory.
   * Before dumping blocks, order files by its name, so that blocks are dumped in an order
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  def dumpBlocks(blocksPath : String) : Unit = {

    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        println(block)
      }
    }

    val blockListener = new BlockListener()
    val reader = new BlockDirectoryReader(blockListener)

    reader.readFrom("/Users/kangmo/crypto/scalachain/src/test/resources/blocks")
  }

  /** The main method of this program. Get the path to directory that has blkNNNNN.dat files, and dump all blocks to stdout.
   *
   * @param args Has only one element, the path to blocks directory.
   */
  def main(args:Array[String]) : Unit = {
    if (args.length != 1) {
      printUsage();
    }
    val blocksPath = args(0)

    dumpBlocks(blocksPath)
  }

  def printUsage(): Unit = {
    "DumpChain <path to the blocks folder which has blkNNNNN.dat files>"
  }
}
