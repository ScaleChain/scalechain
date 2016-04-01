package io.scalechain.blockchain.cli

import java.io.{File, ByteArrayInputStream, ByteArrayOutputStream}

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.script.{BlockPrinterSetter, HashCalculator, ScriptParser, ScriptBytes}
import io.scalechain.blockchain.script.ops._
import io.scalechain.blockchain.storage.{DiskBlockStorage, GenesisBlock, Storage}
import io.scalechain.blockchain.transaction.BlockVerifier
import io.scalechain.util.{ByteArray, HexUtil}
import HexUtil._
import scala.collection._
//import io.scalechain.util.ByteArray
//import ByteArray._

/**
 * Created by kangmo on 11/3/15.
 */
object DumpChain {

  def dump(blocksPath : String, blockListener : BlockReadListener ) : Unit = {
    val reader = new BlockDirectoryReader(blockListener)
    if (!reader.readFrom(blocksPath)) {
      println("The directory that has blkNNNNN.dat files does not exist : " + blocksPath )
    }
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

  /** For loading to Spark, dump all blocks in blkNNNNN.dat files in a directory.
   * Format : block_hash(hex) space raw_block_data(hex)
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  def dumpBlockIndexData(blocksPath : String) : Unit = {

    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        val serializedBlock = BlockCodec.serialize(block)
        val blockHeaderHash = HashCalculator.blockHeaderHash(block.header)

        println(s"${hex(blockHeaderHash)} ${hex(serializedBlock)}")
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


  val DISK_BLOCK_FILE_SIZE = 1024 * 1024 * 128
  def verifyBlocks(blocksPath: String) : Unit = {

    val blockStoragePath = new File("./target/tempblockstorage/")
    blockStoragePath.mkdir()
    val storage = new DiskBlockStorage(blockStoragePath, DISK_BLOCK_FILE_SIZE)
    storage.putBlock( GenesisBlock.BLOCK )

    var blockHeight = 0
    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        storage.putBlock(block)
        new BlockVerifier(block).verify(storage)
        println(s"At block height : $blockHeight, ${BlockVerifier.statistics()}")
        blockHeight += 1
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
    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        println( "bh:"+ block.header )
        implicit val codec = BlockCodec.codec
        val serializedBlock1 = BlockCodec.serialize(block)
        val serializedBlock2 = BlockCodec.serialize( BlockCodec.parse(serializedBlock1) )
        assert( serializedBlock1.sameElements(serializedBlock2) )
      }
    }

    dump( blocksPath, new BlockListener() )
  }

  type ScriptFilter = (UnlockingScript, LockingScript) => Boolean

  /** Merge locking script and unlocking script with filter option.
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   * @param filterOption Specify the type of transaction script. "p2pkh"
   */
  def mergeScripts(blocksPath : String, filterOption : String) : Unit = {
    // Return a filter by checking a specific prefix of the public key in the unlocking script.
    def p2pkh_filter(pkPrefix : Byte) : ScriptFilter = {
      (unlockingScript, lockingScript) => {
        val scriptOps = ScriptParser.parse(unlockingScript)

        if ( scriptOps.operations.length == 2) {
          // Check the second item of unlocking script; PushData(public key)
          scriptOps.operations(1) match {
            case OpPush( _, ScriptBytes( bytes ) ) => {
              println(s"Pattern matched. ${bytes.length}, ${bytes(0)}, ${(bytes.length >= 1) && (bytes(0) == pkPrefix)}")
              // Check prefix
              (bytes.length >= 1) && (bytes(0) == pkPrefix)
            }
            case _ => false
          }
        } else {
          false
        }
      }
    }

    /**
     * Count the number of operations having a given OP code.
     * @param opCode
     * @return
     */
    def locking_script_has(opCode : OpCode) : ScriptFilter = {
      (unlockingScript, lockingScript) => {
        val scriptOps = ScriptParser.parse(lockingScript)
        val count = scriptOps.operations.count( _.opCode() == opCode )
        count > 0
      }
    }

    /* Find out a P2SH transaction */
    def p2sh_filter() : ScriptFilter = {
      (unlockingScript, lockingScript) => {
        val scriptOps = ScriptParser.parse(lockingScript)

        // TODO : Extract the duplicate code to a method. see getRedeemingScript
        scriptOps.operations match {
          case List(OpHash160(), OpPush(20, _), OpEqual()) => {
            true
          }
          case _ => false
        }
      }
    }

    // Create a script filter based on the prefix of the public key.
    val filter : ScriptFilter =
      filterOption match {
        case "p2pkh-uncompressed"    => p2pkh_filter(4)
        case "p2pkh-compressed-even" => p2pkh_filter(2)
        case "p2pkh-compressed-odd"  => p2pkh_filter(3)
        case "multisig-raw"          => locking_script_has(OpCheckMultiSig().opCode())
        case "p2sh"                  => p2sh_filter()
        case ""  => (_,_) => true // No option was specified.
        case _ => (_,_) => false // An invalid option was specified.
      }
    mergeScripts(blocksPath, filter)
  }

  /** Merge locking script and unlocking script to test signature verification.
   *
   * The output of this method can be copied to SignatureCheckingSpec to create a new test case.
   * We will verify all transactions in the block files created by the reference implementation.
   * Whenever we hit an error, we can copy the output of mergeScripts to our test case, SignatureCheckingSpec
   * so that we can automatically test that the issue was fixed.
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   * @param filter the filter to apply. it receives both locking script and unlocking script.
   */
  def mergeScripts(blocksPath : String, filter : ScriptFilter) : Unit = {
    // Step 1) Create a map from transaction hash to Transaction object.
    // c.f. Need List here instead of Array, which implements reference equality.
    val txMap = mutable.Map[ByteArray, Transaction]()

    class BlockListener extends BlockReadListener {
      def onBlock(block: Block): Unit = {
        //println( "bh:"+ block.header )
        for (tx : Transaction <- block.transactions ) {
          //println( "tx:"+tx )
          // Step 2) For each transaction, calculate hash, and put it into the map.
          val txHash = HashCalculator.transactionHash(tx)
          txMap(txHash) = tx

          // Step 3) For each normal transaction input, check if the input transaction exists in the map.
          var inputIndex = 0
          for (txIn : TransactionInput <- tx.inputs) {
            txIn match {
              case normalTxIn : NormalTransactionInput => {
                val txWithOutputOption = txMap.get(normalTxIn.outputTransactionHash.value)
                // Step 4) If it exists, get the locking script from the output of the input transaction.
                //         Get the unlocking script from the transaction input.
                //         Produce a pair ( unlocking script, locking script )
                if (txWithOutputOption.isDefined) { // Found the transaction object from the map.
                  val output = txWithOutputOption.get.outputs(normalTxIn.outputIndex.toInt)

                  val unlockingScript = normalTxIn.unlockingScript
                  val lockingScript   = output.lockingScript

                  // Print the merged script only if the filter returns true.
                  if ( filter(unlockingScript, lockingScript) ) {
                    println(s"MergedScript(transaction=$tx, inputIndex=$inputIndex, unlockingScript=${unlockingScript}, lockingScript=${lockingScript})")
                    println()
                  }
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
    // Enable printing script operations
    BlockPrinterSetter.initialize

    // Initialize the storage subsystem. ex> Load dynamic libraries required for the storage layer.
    Storage.initialize

    if (args.length < 2) {
      printUsage();
    } else {
      val blocksPath = args(0)
      val command = args(1)

      println(s"blocks' path: $blocksPath")
      println(s"command: $command")

      if ( command == "dump-blocks" ) {
        dumpBlocks(blocksPath)
      } else if ( command == "verify-blocks") {
        verifyBlocks(blocksPath)
      } else if ( command == "dump-hashes" ) {
        dumpHashes(blocksPath)
      } else if ( command == "dump-transactions" ) {
        dumpTransactions(blocksPath)
      } else if ( command == "dump-block-index-data" ) {
        dumpBlockIndexData(blocksPath)
      } else if ( command == "verify-serialization" ) {
        verifySerialization(blocksPath)
      } else if ( command == "merge-scripts" ) {
        val option = if (args.length == 3) args(2) else ""
        mergeScripts(blocksPath, option)
      } else {
        printUsage()
      }
    }
  }

  def printUsage(): Unit = {
    println("DumpChain <path to the blocks folder which has blkNNNNN.dat files> <command>");
    println("ex> DumpChain <path> dump-blocks");
    println("ex> DumpChain <path> verify-blocks");
    println("ex> DumpChain <path> dump-hashes");
    println("ex> DumpChain <path> dump-transactions");
    println("ex> DumpChain <path> verify-transactions");
    println("ex> DumpChain <path> dump-block-index-data");
    println("ex> DumpChain <path> verify-serialization");
    println("ex> DumpChain <path> merge-scripts");
    println("ex> DumpChain <path> multisig-raw");
    println("ex> DumpChain <path> p2sh");
  }
}
