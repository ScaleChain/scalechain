package io.scalechain.blockchain.block

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
}

case class NormalTransactionInput(override val outputTransactionHash : TransactionHash,
                                  override val outputIndex : Int,
                                  var unlockingScript : UnlockingScript,
                                  var sequenceNumber : Int) extends TransactionInput {
  override def toString(): String = {
    s"NormalTransactionInput(outputTransactionHash=$outputTransactionHash, outputIndex=$outputIndex, unlockingScript=$unlockingScript, sequenceNumber= $sequenceNumber)"
  }
}

case class GenerationTransactionInput(override val outputTransactionHash : TransactionHash,
                                      override val outputIndex : Int,
                                      val coinbaseData : CoinbaseData,
                                      val sequenceNumber : Int) extends TransactionInput {
  override def toString(): String = {
    s"GenerationTransactionInput(transactionHash=$outputTransactionHash, outputIndex=$outputIndex, coinbaseData=$coinbaseData, sequenceNumber= $sequenceNumber)"
  }
}

class Script(val data:Array[Byte])
{
  def length = data.length
  def apply(i:Int) = data.apply(i)
}

trait LockingScriptPrinter {
  def toString(lockingScript:LockingScript) : String
}
object LockingScript {
  var printer : LockingScriptPrinter = null
}
case class LockingScript(override val data:Array[Byte]) extends Script(data) {
  override def toString(): String = {
    if (LockingScript.printer != null)
      LockingScript.printer.toString(this)
    else
      s"LockingScript(${scalaHex(data)})"
  }
}

trait UnlockingScriptPrinter {
  def toString(unlockingScript:UnlockingScript) : String
}
object UnlockingScript {
  var printer : UnlockingScriptPrinter = null
}
case class UnlockingScript(override val data:Array[Byte]) extends Script(data) {
  override def toString(): String = {
    if (UnlockingScript.printer != null)
      UnlockingScript.printer.toString(this)
    else
      s"UnlockingScript(${scalaHex(data)})"
  }
}

case class TransactionOutput(value : Long, lockingScript : LockingScript) {
  override def toString(): String = {
    s"TransactionOutput(value=${value}L, lockingScript=$lockingScript)"
  }
}

trait TransactionPrinter {
  def toString(transaction:Transaction) : String
}
object Transaction {
  var printer : TransactionPrinter = null
}
case class Transaction(val version : Int,
                       val inputs : Array[TransactionInput],
                       val outputs : Array[TransactionOutput],
                       val lockTime : Int) {

  override def toString() : String = {
    if (Transaction.printer != null)
      Transaction.printer.toString(this)
    else
      s"Transaction(version=$version, inputs=Array(${inputs.mkString(",")}), outputs=Array(${outputs.mkString(",")}), lockTime=$lockTime)"
  }
}


case class Block(val size:Long,
                 val header:BlockHeader,
                 val transactions : Array[Transaction]) {


  override def toString() : String = {
    s"Block(size=$size, header=$header, transactions=Array(${transactions.mkString(",")}))"
  }
}
