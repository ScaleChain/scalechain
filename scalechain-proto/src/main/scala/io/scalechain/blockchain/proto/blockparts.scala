package io.scalechain.blockchain.proto

import io.scalechain.util
import io.scalechain.util.{ByteArray, HexUtil}
import HexUtil.scalaHex
import ByteArray._

import io.scalechain.util.BigIntUtil
import BigIntUtil._
import spray.json.{JsValue, JsString, RootJsonFormat}


abstract class AbstractHash(private val value : ByteArray) extends ProtocolMessage
{
  def isAllZero() = {
    (0 until value.length).forall { i =>
      value(i) == 0
    }
  }

  def toHex() : String = value.toString
}

/** A hash case class that can represent transaction hash or block hash.
  * Used by an inventory vector, InvVector.
  *
  * @param value
  */
case class Hash(value : ByteArray) extends AbstractHash(value) {
  override def toString() = s"Hash($value)"
}
object HashFormat {
  implicit object hashFormat extends RootJsonFormat[Hash] {
    // Instead of { value : "cafebebe" }, we need to serialize the hash to "cafebebe"
    def write(hash : Hash) = JsString( ByteArray.byteArrayToString(hash.value) )

    // Not used.
    def read(value:JsValue) = {
      assert(false)
      null
    }
  }
}


case class BlockHash(value : ByteArray) extends AbstractHash(value) {
  override def toString() = s"BlockHash($value)"
}
case class MerkleRootHash(value : ByteArray) extends AbstractHash(value) {
  override def toString() = s"MerkleRootHash($value)"
}

case class TransactionHash(value : ByteArray) extends AbstractHash(value) {
  override def toString() = s"TransactionHash($value)"
}

case class BlockHeader(version : Int, hashPrevBlock : BlockHash, hashMerkleRoot : MerkleRootHash, timestamp : Long, target : Long, nonce : Long)  extends ProtocolMessage {
  override def toString() : String = {
    s"BlockHeader(version=$version, hashPrevBlock=$hashPrevBlock, hashMerkleRoot=$hashMerkleRoot, timestamp=${timestamp}L, target=${target}L, nonce=${nonce}L)"
  }
}

case class CoinbaseData(data: ByteArray) extends ProtocolMessage {
  override def toString() : String = {
    s"CoinbaseData($data)"
  }
}

trait TransactionInput extends ProtocolMessage {
  val outputTransactionHash : TransactionHash
  val outputIndex : Long
}

case class NormalTransactionInput(override val outputTransactionHash : TransactionHash,
                                  // BUGBUG : Change to Int
                                  override val outputIndex : Long,
                                  val unlockingScript : UnlockingScript,
                                  val sequenceNumber : Long) extends TransactionInput {
  override def toString(): String = {
    s"NormalTransactionInput(outputTransactionHash=$outputTransactionHash, outputIndex=${outputIndex}L, unlockingScript=$unlockingScript, sequenceNumber=${sequenceNumber}L)"
  }
}

case class GenerationTransactionInput(override val outputTransactionHash : TransactionHash,
                                      // BUGBUG : Change to Int
                                      override val outputIndex : Long,
                                      val coinbaseData : CoinbaseData,
                                      val sequenceNumber : Long) extends TransactionInput {
  override def toString(): String = {
    s"GenerationTransactionInput(transactionHash=$outputTransactionHash, outputIndex=${outputIndex}L, coinbaseData=$coinbaseData, sequenceNumber= ${sequenceNumber}L)"
  }
}

class Script(val data:ByteArray) extends ProtocolMessage
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
case class LockingScript(override val data:ByteArray) extends Script(data) {
  override def toString(): String = {
    if (LockingScript.printer != null)
      LockingScript.printer.toString(this)
    else
      s"LockingScript($data)"
  }
}

trait UnlockingScriptPrinter {
  def toString(unlockingScript:UnlockingScript) : String
}
object UnlockingScript {
  var printer : UnlockingScriptPrinter = null
}
case class UnlockingScript(override val data:ByteArray) extends Script(data) {
  override def toString(): String = {
    if (UnlockingScript.printer != null)
      UnlockingScript.printer.toString(this)
    else
      s"UnlockingScript($data)"
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
                       val inputs : List[TransactionInput],
                       val outputs : List[TransactionOutput],
                       val lockTime : Long) extends ProtocolMessage {

  override def toString() : String = {
    if (Transaction.printer != null)
      Transaction.printer.toString(this)
    else
      s"Transaction(version=$version, inputs=List(${inputs.mkString(",")}), outputs=List(${outputs.mkString(",")}), lockTime=${lockTime}L)"
  }
}

/** The block message is sent in response to a getdata message
  * which requests transaction information from a block hash.
  */
case class Block(val header:BlockHeader,
                 val transactions : List[Transaction]) extends ProtocolMessage {


  override def toString() : String = {
    s"Block(header=$header, transactions=List(${transactions.mkString(",")}))"
  }
}

/** IPv6 address. Network byte order.
  * The original client only supported IPv4 and only read the last 4 bytes to get the IPv4 address.
  * However, the IPv4 address is written into the message as a 16 byte IPv4-mapped IPv6 address
  */
case class IPv6Address(address:ByteArray) extends ProtocolMessage {
  override def toString() : String = {
    s"IPv6Address($address)"
  }
  def inetAddress = com.google.common.net.InetAddresses.fromLittleEndianByteArray(address.array.reverse)
}

// TODO : Add a comment
case class NetworkAddress(services:BigInt, ipv6:IPv6Address, port:Int) extends ProtocolMessage {
  override def toString() : String = {
    s"NetworkAddress(${bint(services)}, $ipv6, $port)"
  }
}
// TODO : Add a comment
case class NetworkAddressWithTimestamp(timestamp:Long, address:NetworkAddress) extends ProtocolMessage {
  override def toString() : String = {
    s"NetworkAddressWithTimestamp(${timestamp}L, $address)"
  }
}

