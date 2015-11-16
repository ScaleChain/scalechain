package io.scalechain.blockchain

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import io.scalechain.blockchain.block._
import scala.collection._

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
            println( "tx:"+ input.outputTransactionHash.toHex);
          }
        }
      }
    }

    dump( blocksPath, new BlockListener() )
  }

  /** Dump all transactions.
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
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


  /** Verify that serializing parsed data produces the original serialized data.
   * This is to make sure that the serialize and parse method works correctly.
   *
   * In other words, (A) and (B) should be same in the following data flow.
   *   parse -> serialize -> (A) -> parse -> serialize -> (B)
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  def verifySerialization(blocksPath: String) : Unit = {
    def serialize(block : Block) : Array[Byte] = {
      val bout = new ByteArrayOutputStream()
      val dout = new BlockDataOutputStream(bout)
      try {
        val serializer = new BlockSerializer(dout)
        serializer.serialize(block)
      } finally {
        dout.close()
      }
      bout.toByteArray
    }

    def parse(data: Array[Byte]) : Block = {
      val din = new BlockDataInputStream( new ByteArrayInputStream(data))
      val parser = new BlockParser(din)
      parser.parse().get
    }

    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        println( "bh:"+ block.header )
        val serializedBlock1 = serialize(block)
        val serializedBlock2 = serialize( parse(serializedBlock1) )
        assert( serializedBlock1.sameElements(serializedBlock2) )
      }
    }

    dump( blocksPath, new BlockListener() )
  }

  /** Merge locking script and unlocking script to test signature verification.
   *
   * The output of this method can be copied to SignatureCheckingSpec to create a new test case.
   * We will verify all transactions in the block files created by the reference implementation.
   * Whenever we hit an error, we can copy the output of mergeScripts to our test case, SignatureCheckingSpec
   * so that we can automatically test that the issue was fixed.
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  def mergeScripts(blocksPath : String) : Unit = {
    // Step 1) Create a map from transaction hash to Transaction object.
    // c.f. Need List here instead of Array, which implements reference equality.
    val txMap = mutable.Map[List[Byte], Transaction]()

    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        //println( "bh:"+ block.header )
        for (tx : Transaction <- block.transactions ) {
          //println( "tx:"+tx )
          // Step 2) For each transaction, calculate hash, and put it into the map.
          val txHash = HashCalculator.transactionHash(tx).toList
          txMap(txHash) = tx

          // Step 3) For each normal transaction input, check if the input transaction exists in the map.
          var inputIndex = 0
          for (txIn : TransactionInput <- tx.inputs) {
            txIn match {
              case normalTxIn : NormalTransactionInput => {
                val txWithOutputOption = txMap.get(normalTxIn.outputTransactionHash.hash.toList)
                // Step 4) If it exists, get the locking script from the output of the input transaction.
                //         Get the unlocking script from the transaction input.
                //         Produce a pair ( unlocking script, locking script )
                if (txWithOutputOption.isDefined) { // Found the transaction object from the map.
                  val output = txWithOutputOption.get.outputs(normalTxIn.outputIndex)
                  println(s"MergedScript(transaction=$tx, inputIndex=$inputIndex, unlockingScript=${normalTxIn.unlockingScript}, lockingScript=${output.lockingScript})")
                  println()
                }
              }
              case _ => {
                // Do nothing for GenerationTransactionInput.
              }
            }
            inputIndex += 1
          }
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
      } else if ( command == "verify-serialization" ) {
        verifySerialization(blocksPath)
      } else if ( command == "merge-scripts" ) {
        mergeScripts(blocksPath)
      } else {
        printUsage()
      }
    }
  }

  def printUsage(): Unit = {
    println("DumpChain <path to the blocks folder which has blkNNNNN.dat files> <command>");
    println("ex> DumpChain <path> dump-blocks");
    println("ex> DumpChain <path> dump-hashes");
    println("ex> DumpChain <path> dump-transactions");
    println("ex> DumpChain <path> verify-serialization");
    println("ex> DumpChain <path> merge-scripts");
  }
}
