package io.scalechain.blockchain.cli

import java.io.File

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.script.BlockPrinterSetter
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.script.ops.*
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.DiskBlockStorage
import io.scalechain.blockchain.storage.GenesisBlock
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.transaction.BlockVerifier
import io.scalechain.util.HexUtil
import java.util.*


interface ScriptFilter {
  fun filterByScripts( unlockingScript : UnlockingScript, lockingScript : LockingScript) : Boolean
}

/**
 * Created by kangmo on 11/3/15.
 */
object DumpChain {

  fun dump(blocksPath : String, blockListener : BlockReadListener ) : Unit {
    val reader = BlockDirectoryReader(blockListener)
    if (!reader.readFrom(blocksPath)) {
      println("The directory that has blkNNNNN.dat files does not exist : " + blocksPath )
    }
  }


  /** Dump all blocks in blkNNNNN.dat files in a directory.
   * Before dumping blocks, order files by its name, so that blocks are dumped in an order
    *
    * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  fun dumpBlocks(blocksPath : String) : Unit {

    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        println(block)
      }
    }

    dump( blocksPath, BlockListener() )
  }

  /** For loading to Spark, dump all blocks in blkNNNNN.dat files in a directory.
   * Format : block_hash(hex) space raw_block_data(hex)
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  fun dumpBlockIndexData(blocksPath : String) : Unit {

    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        val serializedBlock = BlockCodec.encode(block)
        val blockHeaderHash = block.header.hash()

        println("${HexUtil.hex(blockHeaderHash.value)} ${HexUtil.hex(serializedBlock)}")
      }
    }

    dump( blocksPath, BlockListener() )
  }

  /** Dump all hash values.
   * tx : transaction hash
   * bk : block hash
   * mk : merkle root hash
    *
    * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  fun dumpHashes(blocksPath : String) : Unit {
    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        println( "bk:"+ HexUtil.hex(block.header.hashPrevBlock.value) );
        println( "mk:"+ HexUtil.hex(block.header.hashMerkleRoot.value) );
        for (tx : Transaction in block.transactions ) {
          for (input : TransactionInput in tx.inputs ) {
            println( "tx:"+ HexUtil.hex(input.outputTransactionHash.value));
          }
        }
      }
    }

    dump( blocksPath, BlockListener() )
  }

  /** Dump all transactions.
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  fun dumpTransactions(blocksPath: String) : Unit {
    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        println( "bh:"+ block.header );
        for (tx : Transaction in block.transactions ) {
          println( "tx:"+tx )
        }
      }
    }

    dump( blocksPath, BlockListener() )
  }


  val DISK_BLOCK_FILE_SIZE = 1024 * 1024 * 128
  fun verifyBlocks(blocksPath: String) : Unit {

    val blockStoragePath = File("./target/tempblockstorage/")
    blockStoragePath.mkdir()

    val db = RocksDatabase(blockStoragePath)
    val storage = DiskBlockStorage(db, blockStoragePath, DISK_BLOCK_FILE_SIZE)
    val chain = Blockchain.create(db, storage)
    BlockProcessor.create(chain)
    chain.putBlock( db, GenesisBlock.HASH, GenesisBlock.BLOCK )

    var blockHeight = 0
    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        storage.putBlock(db, block)
        BlockVerifier(db, block).verify(chain)
        println("At block height : $blockHeight, ${BlockVerifier.statistics()}")
        blockHeight += 1
      }
    }

    dump( blocksPath, BlockListener() )
  }



  /** Verify that serializing parsed data produces the original serialized data.
   * This is to make sure that the serialize and parse method works correctly.
   *
   * In other words, (A) and (B) should be same in the following data flow.
   *   parse -> serialize -> (A) -> parse -> serialize -> (B)
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   */
  fun verifySerialization(blocksPath: String) : Unit {
    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        println( "bh:"+ block.header )
        val serializedBlock1 = BlockCodec.encode(block)
        val serializedBlock2 = BlockCodec.encode( BlockCodec.decode(serializedBlock1)!! )
        assert( Arrays.equals(serializedBlock1, serializedBlock2)  )
      }
    }

    dump( blocksPath, BlockListener() )
  }

  // Starting from Kotlin 1.1, typealias is supported.
  // typealias ScriptFilter = ((UnlockingScript, LockingScript) -> Boolean)


  /** Merge locking script and unlocking script with filter option.
   *
   * @param blocksPath path to the blocks directory which has blkNNNNN.dat files.
   * @param filterOption Specify the type of transaction script. "p2pkh"
   */
  fun mergeScripts(blocksPath : String, filterOption : String) : Unit {
    // Return a filter by checking a specific prefix of the public key in the unlocking script.
    fun p2pkh_filter(pkPrefix : Byte) : ScriptFilter {
      return object : ScriptFilter {
        override fun filterByScripts(unlockingScript: UnlockingScript, lockingScript : LockingScript) : Boolean {
          val scriptOps = ScriptParser.parse(unlockingScript)

          if ( scriptOps.operations.size == 2) {
            // Check the second item of unlocking script; PushData(public key)
            val operation = scriptOps.operations[1]
            when {
              operation is OpPush -> {
                val bytes = operation.inputValue!!.value
                println("Pattern matched. ${bytes.size}, ${bytes[0]}, ${(bytes.size >= 1) && (bytes[0] == pkPrefix)}")
                // Check prefix
                return (bytes.size >= 1) && (bytes[0] == pkPrefix)
              }
              else -> return false
            }
          } else {
            return false
          }
        }
      }
    }

    /**
     * Count the number of operations having a given OP code.
      *
      * @param opCode
     * @return
     */
    fun locking_script_has(opCode : OpCode) : ScriptFilter {
      return object : ScriptFilter {
        override fun filterByScripts(unlockingScript: UnlockingScript, lockingScript : LockingScript) : Boolean {
          val scriptOps = ScriptParser.parse(lockingScript)
          val count = scriptOps.operations.count { it.opCode() == opCode }
          return count > 0
        }
      }
    }

    /* Find out a P2SH transaction */
    fun p2sh_filter() : ScriptFilter {
      return object : ScriptFilter {
        override fun filterByScripts(unlockingScript: UnlockingScript, lockingScript : LockingScript) : Boolean {
          val scriptOps = ScriptParser.parse(lockingScript)

          if (scriptOps.operations.size == 3) {
            // TODO : Extract the duplicate code to a method. see getRedeemingScript

            val opHash160 = scriptOps.operations[0]
            val opPush = scriptOps.operations[1]
            val opEqual = scriptOps.operations[2]
            if (opHash160 is OpHash160 &&
                opPush is OpPush &&
                opEqual is OpEqual) {
              return opPush.byteCount == 20
            } else {
              return false
            }
          } else {
            return false
          }
        }
      }
    }

    // Create a script filter based on the prefix of the public key.
    val filter : ScriptFilter =
      when(filterOption) {
        "p2pkh-uncompressed"    -> p2pkh_filter(4)
        "p2pkh-compressed-even" -> p2pkh_filter(2)
        "p2pkh-compressed-odd"  -> p2pkh_filter(3)
        "multisig-raw"          -> locking_script_has(OpCheckMultiSig().opCode())
        "p2sh"                  -> p2sh_filter()
        ""                      -> object : ScriptFilter { // No option was specified.
          override fun filterByScripts(unlockingScript: UnlockingScript, lockingScript: LockingScript) : Boolean = true
        }
        else                    -> object : ScriptFilter { // An invalid option was specified.
          override fun filterByScripts(unlockingScript: UnlockingScript, lockingScript: LockingScript) : Boolean = false
        }
      }
    return mergeScripts(blocksPath, filter)
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
  fun mergeScripts(blocksPath : String, filter : ScriptFilter) : Unit {
    // Step 1) Create a map from transaction hash to Transaction object.
    // c.f. Need List here instead of Array, which implements reference equality.
    val txMap = mutableMapOf<ByteArray, Transaction>()

    class BlockListener : BlockReadListener {
      override fun onBlock(block: Block): Unit {
        //println( "bh:"+ block.header )
        for (tx : Transaction in block.transactions ) {
          //println( "tx:"+tx )
          // Step 2) For each transaction, calculate hash, and put it into the map.
          txMap[tx.hash().value] = tx

          // Step 3) For each normal transaction input, check if the input transaction exists in the map.
          var inputIndex = 0
          for (txIn : TransactionInput in tx.inputs) {
            when {
              txIn is NormalTransactionInput -> {
                val txWithOutputOption = txMap.get(txIn.outputTransactionHash.value)
                // Step 4) If it exists, get the locking script from the output of the input transaction.
                //         Get the unlocking script from the transaction input.
                //         Produce a pair ( unlocking script, locking script )
                if (txWithOutputOption != null) { // Found the transaction object from the map.
                  val output = txWithOutputOption.outputs[txIn.outputIndex.toInt()]

                  val unlockingScript = txIn.unlockingScript
                  val lockingScript   = output.lockingScript

                  // Print the merged script only if the filter returns true.
                  if ( filter.filterByScripts(unlockingScript, lockingScript) ) {
                    println("MergedScript(transaction=$tx, inputIndex=$inputIndex, unlockingScript=${unlockingScript}, lockingScript=${lockingScript})")
                    println()
                  }
                }
              }
              else -> {
                // Do nothing for GenerationTransactionInput.
              }
            }
            inputIndex += 1
          }
        }
      }
    }

    dump( blocksPath, BlockListener() )
  }

  /** The main method of this program. Get the path to directory that has blkNNNNN.dat files, and dump all blocks to stdout.
   *
   * @param args Has only one element, the path to blocks directory.
   */
  fun main(args:Array<String>) : Unit {
    // Enable printing script operations
    BlockPrinterSetter.initialize()

    // Initialize the storage subsystem. ex> Load dynamic libraries required for the storage layer.
    Storage.initialize()

    if (args.size < 2) {
      printUsage();
    } else {
      val blocksPath = args[0]
      val command = args[1]

      println("blocks' path: $blocksPath")
      println("command: $command")

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
        val option = if (args.size == 3) args[2] else ""
        mergeScripts(blocksPath, option)
      } else {
        printUsage()
      }
    }
  }

  fun printUsage(): Unit {
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
