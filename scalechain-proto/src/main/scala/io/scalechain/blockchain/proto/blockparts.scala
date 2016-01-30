package io.scalechain.blockchain.proto

import io.scalechain.util.HexUtil
import HexUtil.scalaHex


/**
  * Created by kangmo on 2015. 11. 1..
  */

case class Timestamp(val unixTimestamp : Int) extends ProtocolMessage

abstract class Hash(private val hash : Array[Byte]) extends ProtocolMessage
{
  def isAllZero() = {
    (0 until hash.length).forall { i =>
      hash(i) == 0
    }
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

case class BlockHeader(val version : Int, hashPrevBlock : BlockHash, hashMerkleRoot : MerkleRootHash, time : Timestamp, target : Int, nonce : Int)  extends ProtocolMessage {
  override def toString() : String = {
    s"BlockHeader(version=$version, hashPrevBlock=$hashPrevBlock, hashMerkleRoot=$hashMerkleRoot, time=$time, target= $target, nonce= $nonce)"
  }
}

case class CoinbaseData(data: Array[Byte]) extends ProtocolMessage {
  override def toString() : String = {
    s"CoinbaseData(${scalaHex(data)})"
  }
}

trait TransactionInput extends ProtocolMessage {
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

class Script(val data:Array[Byte]) extends ProtocolMessage
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

case class TransactionOutput(value : Long, lockingScript : LockingScript) extends ProtocolMessage {
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

/** Tx ; tx describes a bitcoin transaction, in reply to getdata.
  */
case class Transaction(val version : Int,
                       val inputs : Array[TransactionInput],
                       val outputs : Array[TransactionOutput],
                       val lockTime : Int) extends ProtocolMessage {

  override def toString() : String = {
    if (Transaction.printer != null)
      Transaction.printer.toString(this)
    else
      s"Transaction(version=$version, inputs=Array(${inputs.mkString(",")}), outputs=Array(${outputs.mkString(",")}), lockTime=$lockTime)"
  }
}

/** The block message is sent in response to a getdata message
  * which requests transaction information from a block hash.
  */
case class Block(val size:Long,
                 val header:BlockHeader,
                 val transactions : Array[Transaction]) extends ProtocolMessage {


  override def toString() : String = {
    s"Block(size=$size, header=$header, transactions=Array(${transactions.mkString(",")}))"
  }
}