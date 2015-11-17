package io.scalechain.blockchain.block

import java.io._

import io.scalechain.blockchain.block.index.BlockIndex
import io.scalechain.blockchain.{ScriptEvalException, ScriptParseException, ErrorCode, TransactionVerificationException}
import io.scalechain.blockchain.script.{ScriptOpList, ScriptInterpreter, ScriptParser, ScriptEnvironment}
import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.blockchain.util.Utils
import io.scalechain.util.{Hash256, Hash}
import io.scalechain.util.HexUtil._


/**
 * Created by kangmo on 2015. 11. 1..
 */

case class Timestamp(val unixTimestamp : Int)

abstract class Hash(private val hash : Array[Byte])
{
  def isAllZero() = {
    // BUGBUG : Dirty code. make it cleaner!
    var countOfZero = 0
    for ( byteValue : Byte <- hash ) {
      if (byteValue == 0)
        countOfZero += 1
    }
    (countOfZero == hash.length)
  }

  def toHex() : String = {
    s"${scalaHex(hash.reverse)}"
  }
}

case class BlockHash(val hash : Array[Byte]) extends Hash(hash) {
  override def toString() : String = {
    s"BlockHash(${scalaHex(hash)}))"
  }
}
case class MerkleRootHash(val hash : Array[Byte]) extends Hash(hash) {
  override def toString() : String = {
    s"MerkleRootHash(${scalaHex(hash)})"
  }
}

case class TransactionHash(val hash : Array[Byte]) extends Hash(hash) {
  override def toString() : String = {
    s"TransactionHash(${scalaHex(hash)})"
  }
}

case class BlockHeader(val version : Int, hashPrevBlock : BlockHash, hashMerkleRoot : MerkleRootHash, time : Timestamp, target : Int, nonce : Int) {
  override def toString() : String = {
    s"BlockHeader(version=$version, hashPrevBlock=$hashPrevBlock, hashMerkleRoot=$hashMerkleRoot, time=$time, target= $target, nonce= $nonce)"
  }
}

case class CoinbaseData(data: Array[Byte]) {
  override def toString() : String = {
    s"CoinbaseData(${scalaHex(data)})"
  }
}

trait TransactionInput {
  val outputTransactionHash : TransactionHash
  val outputIndex : Int

  /** Verify that the unlocking script of the input successfully unlocks the locking script attached to the UTXO that this input references.
   *
   * @param env The script execution environment, which has (1) the transaction that this input belongs to, and (2) the index of the inputs of the transaction that corresponds to this transaction input.
   * @param blockIndex A block index that can search a transaction by its hash.
   *
   * @throws TransactionVerificationException if the verification failed.
   */
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex): Unit
}

case class NormalTransactionInput(override val outputTransactionHash : TransactionHash,
                                  override val outputIndex : Int,
                                  var unlockingScript : UnlockingScript,
                                  var sequenceNumber : Int) extends TransactionInput {
  override def toString(): String = {
    s"NormalTransactionInput(outputTransactionHash=$outputTransactionHash, outputIndex=$outputIndex, unlockingScript=$unlockingScript, sequenceNumber= $sequenceNumber)"
  }

  /** Convert any exception happened within the body to TransactionVerificationException
   *
   * @param body A function that parses and executes scripts.
   * @tparam T The return type of the body function
   * @return Whatever the body returns.
   */
  protected def throwingTransactionVerificationException[T]( body : => T ) : T = {
    try {
      body
    } catch {
      case e : TransactionVerificationException => {
        throw e
      }
      case e : ScriptParseException => {
        throw new TransactionVerificationException(ErrorCode.ScriptParseFailure, e.getMessage(), e.getStackTrace())
      }
      case e : ScriptEvalException => {
        throw new TransactionVerificationException(ErrorCode.ScriptEvalFailure, e.getMessage(), e.getStackTrace())
      }
      case e : Exception => {
        throw new TransactionVerificationException(ErrorCode.GeneralFailure, e.getMessage(), e.getStackTrace())
      }
    }
  }

  /** Get the locking script attached to the UTXO which this input references with transactionHash and outputIndex.
   *
   * @param blockIndex A block index that can search a transaction by its hash.
   *
   * @return The locking script attached to the UTXO which this input references.
   */
  def getLockingScript(blockIndex : BlockIndex): LockingScript = {
    val outputTxOption = blockIndex.getTransaction(outputTransactionHash)
    if (outputTxOption.isEmpty) {
      // The transaction which produced the UTXO does not exist.
      throw new TransactionVerificationException(ErrorCode.InvalidOutputTransactionHash)
    }
    // The transaction that produced UTXO exists.
    val outputTx = outputTxOption.get
    if (outputIndex < 0 || outputIndex >= outputTx.outputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidOutputIndex)
    }

    /**
     *  BUGBUG : Need to check if the UTXO is from Generation transaction to check 100 blocks are created?
     */

    outputTx.outputs(outputIndex).lockingScript
  }

  /** With a block index, verify that the unlocking script of the input successfully unlocks the locking script attached to the UTXO that this input references.
    *
    * @param env The script execution environment, which has (1) the transaction that this input belongs to, and (2) the index of the inputs of the transaction that corresponds to this transaction input.
    * @param blockIndex A block index that can search a transaction by its hash.
    * @throws TransactionVerificationException if the verification failed.
    */
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex): Unit = {
    throwingTransactionVerificationException {
      val lockingScript : LockingScript = getLockingScript(blockIndex)

      verify(env, lockingScript)
    }
  }

  /** With a locking script, verify that the unlocking script of the input successfully unlocks the locking script attached to the UTXO that this input references.
   *
   * @param env The script execution environment, which has (1) the transaction that this input belongs to, and (2) the index of the inputs of the transaction that corresponds to this transaction input.
   * @param lockingScript The locking script attached to the UTXO that this input references.
   */
  def verify(env : ScriptEnvironment, lockingScript : LockingScript): Unit = {
    throwingTransactionVerificationException {
      // Step 1 : Parse and run the unlocking script
      val unlockingScriptOps : ScriptOpList = ScriptParser.parse(unlockingScript)
      ScriptInterpreter.eval_internal(env, unlockingScriptOps)

      // Step 2 : Parse and run the locking script
      val lockingScriptOps : ScriptOpList = ScriptParser.parse(lockingScript)
      ScriptInterpreter.eval_internal(env, lockingScriptOps)

      // Step 3 : Check the top value of the stack
      val top = env.stack.pop()
      if ( !Utils.castToBool(top.value) ) {
        throw new TransactionVerificationException(ErrorCode.TopValueFalse)
      }
    }
  }

}

case class GenerationTransactionInput(override val outputTransactionHash : TransactionHash,
                                      override val outputIndex : Int,
                                      val coinbaseData : CoinbaseData,
                                      val sequenceNumber : Int) extends TransactionInput {
  override def toString(): String = {
    s"GenerationTransactionInput(transactionHash=$outputTransactionHash, outputIndex=$outputIndex, coinbaseData=$coinbaseData, sequenceNumber= $sequenceNumber)"
  }

  /** Verify that 100 blocks are created after the generation transaction was created.
    * Generation transactions do not reference any UTXO, as it creates UTXO from the scratch.
    * So, we don't have to verify the locking script and unlocking script, but we need to make sure that at least 100 blocks are created
    * after the block where this generation transaction exists.
    *
    * @param env For a generation transaction, this is null, because we don't need to execute any script.
    * @param blockIndex For a generation tranasction, this is null, because we don't need to search any tranasction by its hash.
    *
    * @throws TransactionVerificationException if the verification failed.
    */
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex): Unit = {
    assert(env == null)
    assert(blockIndex == null)
    // Do nothing.
  }
}

class Script(val data:Array[Byte])
{
  def length = data.length
  def apply(i:Int) = data.apply(i)
}

case class LockingScript(override val data:Array[Byte]) extends Script(data) {
  override def toString(): String = {
    val scriptOps = ScriptParser.parse(this)

    s"LockingScript(${scalaHex(data)}) /* ops:$scriptOps */ "
  }
}

case class UnlockingScript(override val data:Array[Byte]) extends Script(data) {
  override def toString(): String = {
    val scriptOps = ScriptParser.parse(this)

    // The last byte of the signature, hash type decides how to create a hash value from transaction and script.
    // The hash value and public key is used to verify the signature.
    val hashType = scriptOps.operations(0) match {
      case signature : OpPush => {
        Some(signature.inputValue.value.last)
      }
      case _ => {
        None
      }
    }

    s"UnlockingScript(${scalaHex(data)}) /* ops:$scriptOps, hashType:$hashType */"
  }
}


case class TransactionOutput(value : Long, lockingScript : LockingScript) {
  override def toString(): String = {
    s"TransactionOutput(value=${value}L, lockingScript=$lockingScript)"
  }
}

case class Transaction(version : Int,
                       inputs : Array[TransactionInput],
                       outputs : Array[TransactionOutput],
                       lockTime : Int) {

  override def toString() : String = {
    // TODO : HashCalculator depends on Transaction, and also Transaction depends on HashCalculator. Get rid of the circular dependency.
    s"Transaction(version=$version, inputs=Array(${inputs.mkString(",")}), outputs=Array(${outputs.mkString(",")}), lockTime=$lockTime /* hash:${scalaHex(HashCalculator.transactionHash(this))} */)"
  }


  /** Calculate hash value for a given transaction input, and part of script that unlocks the UTXO attached to the input.
   * Why use part of script instead of all script bytes?
   *
   * 1. We need to use bytes after the OP_CODESEPARATOR in the script.
   * 2. We need to get rid of all signature data from the script.
   * 3. We need to get rid of OP_CODESEPARATOR OP code from the script.
   *
   * @param transactionInputIndex The index of the transaction input to get the hash.
   * @param scriptData A part of unlocking script for the UTXO attached to the given transaction input.
   * @param howToHash Decides how to calculate the hash value from this transaction and the given script.
   *                  The value should be one of values in Transaction.SigHash
   * @return The calculated hash value.
   */
  def hashForSignature(transactionInputIndex : Int, scriptData : Array[Byte], howToHash : Int) : Hash256 = {
    // Step 1 : Check if the transactionInputIndex is valid.
    if (transactionInputIndex < 0 || transactionInputIndex >= inputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidInputIndex)
    }

    // Step 2: copy each field of this transaction and create a new one.
    //         Why? To change some fields of the transaction to calculate hash value from it.
    val transaction = this.copy()

    // Step 3 : For each hash type, mutate the transaction.
    transaction.alter(transactionInputIndex, scriptData, howToHash)

    // Step 4 : calculate hash of the transaction.
    transaction.calculateHash(howToHash)
  }

  /** Utility function : For each normal transaction, call a mutate function.
   * Pass the index to the inputs array and normal transaction input to the mutate function.
   *
   * @param mutate A function with two parameters. (1) transaction input index (2) normal transaction input
   */
  protected def forEachNormalTransaction(mutate : (Int, NormalTransactionInput) => Unit) : Unit = {
    var txIndex = 0
    for (txInput : TransactionInput <- inputs) {
      txInput match {
        case normalTxInput : NormalTransactionInput => {
          mutate(txIndex, normalTxInput)
        }
        case _ => {
          // nothing to do for the generation transaction.
        }
      }
      txIndex += 1
    }
  }

  /** Alter transaction inputs to calculate hash value used for signing/verifying a signature.
   *
   * @param transactionInputIndex See hashForSignature
   * @param scriptData See hashForSignature
   * @param howToHash See hashForSignature
   */
  protected def alter(transactionInputIndex : Int, scriptData : Array[Byte], howToHash : Int) : Unit = {
    howToHash match {
      case Transaction.SigHash.ALL => {
        // Set an empty unlocking script for all inputs
        forEachNormalTransaction { (txIndex, normalTxInput) =>
          normalTxInput.unlockingScript =
            if (txIndex == transactionInputIndex)
              UnlockingScript(scriptData)
            else
              UnlockingScript(Array())
        }
      }
      case Transaction.SigHash.NONE => {
        throw new TransactionVerificationException(ErrorCode.UnsupportedHashType)
      }
      case Transaction.SigHash.SINGLE => {
        throw new TransactionVerificationException(ErrorCode.UnsupportedHashType)
      }
    }
  }

  /** Calculate hash value of this transaction for signing/validating a signature.
   *
   * @param howToHash The hash type. A value of Transaction.SigHash.
   * @return The calcuated hash.
   */
  protected def calculateHash(howToHash : Int) : Hash256 = {

    val bout = new ByteArrayOutputStream()
    val dout = new BlockDataOutputStream(bout)
    try {
      val serializer = new BlockSerializer(dout)
      // Step 1 : Serialize the transaction
      serializer.writeTransaction(this)
      // Step 2 : Write hash type
      Utils.uint32ToByteStreamLE(0x000000ff & howToHash, dout);
    } finally {
      dout.close()
    }
    // Step 3 : Calculate hash
    Hash.hash256(bout.toByteArray)
  }

}

object Transaction {
  object SigHash {
    val ALL = 1
    val NONE = 2
    val SINGLE = 3
  }
  val ANYONECANPAY : Int = 0x80
}

case class Block(val size:Long,
                 val header:BlockHeader,
                 val transactions : Array[Transaction]) {


  override def toString() : String = {
    s"Block(size=$size, header=$header, transactions=Array(${transactions.mkString(",")}))"
  }
}

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
