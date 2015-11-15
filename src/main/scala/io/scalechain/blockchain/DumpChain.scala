package io.scalechain.blockchain

import io.scalechain.blockchain.block._

/**
 * Created by kangmo on 11/3/15.
 */
object DumpChain {

  def dump(blocksPath : String, blockListener : BlockReadListener ) : Unit = {
    val reader = new BlockDirectoryReader(blockListener)
    reader.readFrom(blocksPath)
  }


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

    dump( blocksPath, new BlockListener() )
  }

  /** Dump all hash values.
   * tx : transaction hash
   * bk : block hash
   * mk : merkle root hash
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  def dumpHashes(blocksPath : String) : Unit = {
    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        println( "bk:"+ block.header.hashPrevBlock.toHex );
        println( "mk:"+ block.header.hashMerkleRoot.toHex );
        for (tx : Transaction <- block.transactions ) {
          for (input : TransactionInput <- tx.inputs ) {
            println( "tx:"+ input.transactionHash.toHex);
          }
        }
      }
    }

    dump( blocksPath, new BlockListener() )
  }

  /** Dump all transactions.
   *
   * @param blocksPath
   */
  def dumpTransactions(blocksPath: String) : Unit = {
    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        println( "bh:"+ block.header );
        for (tx : Transaction <- block.transactions ) {
          println( "tx:"+tx )
        }
      }
    }

    dump( blocksPath, new BlockListener() )
  }

  /** The main method of this program. Get the path to directory that has blkNNNNN.dat files, and dump all blocks to stdout.
   *
   * @param args Has only one element, the path to blocks directory.
   */
  def main(args:Array[String]) : Unit = {
    if (args.length != 2) {
      printUsage();
    } else {
      val blocksPath = args(0)
      val command = args(1)

      if ( command == "dump-blocks" ) {
        dumpBlocks(blocksPath)
      } else if ( command == "dump-hashes" ) {
        dumpHashes(blocksPath)
      } else if ( command == "dump-transactions" ) {
        dumpTransactions(blocksPath)
      }
    }
  }

  def printUsage(): Unit = {
    println("DumpChain <path to the blocks folder which has blkNNNNN.dat files> <command>");
    println("ex> DumpChain <path> dump-blocks");
    println("ex> DumpChain <path> dump-hashes");
    println("ex> DumpChain <path> dump-transactions");
  }
}
