package io.scalechain.blockchain.proto.walletparts

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.ByteArray
import spray.json.{JsValue, JsString, RootJsonFormat}

/**
  * Created by mijeong on 2016. 3. 22..
  */

abstract class AbstractHash(private val value : ByteArray) extends ProtocolMessage
{
  def isAllZero() = {
    (0 until value.length).forall { i =>
      value(i) == 0
    }
  }

  def toHex() : String = value.toString
}

/** A hash case class that can represent account hash or address hash.
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

case class AccountHash(value : ByteArray) extends AbstractHash(value) {
  override def toString() = s"AccountHash($value)"
}

case class AddressHash(value : ByteArray) extends AbstractHash(value) {
  override def toString() = s"AddressHash($value)"
}

/**
  *
  */
case class Account(header: AccountHeader,
                   account: String,
                   addresses : List[Address]) extends ProtocolMessage {


  override def toString() : String = {
    s"Account(header=${header}, account=${account}, addresses=List(${addresses.mkString(",")}))"
  }
}

case class AccountHeader(version : Int,
                         timestamp : Long)  extends ProtocolMessage {

  override def toString() : String = {
    s"AccountHeader(version=${version}L, timestamp=${timestamp}L)"
  }
}

case class Address(privateKey: ByteArray,
                   publicKey: ByteArray,
                   purpose: Int,
                   address : String
                   )  extends ProtocolMessage {

  override def toString() : String = {
    s"Address(privateKey=${privateKey}, publicKey=${publicKey}, purpose=${purpose}, address=${address})"
  }
}

case class WalletTransaction(header : WalletTransactionHeader,
                             transactions : List[WalletTransactionDetail]) extends ProtocolMessage {
  override def toString() : String = {
    s"WalletTransaction(header=${header}, transactions=List(${transactions.mkString(",")}))"
  }
}

case class WalletTransactionHeader(version : Int,
                                   height : Int) extends ProtocolMessage {
  override def toString() : String = {
    s"WalletTransactionHeader(version=${version}, height=${height})"
  }
}

case class WalletTransactionDetail(account : String,
                             address : String,
                             toAddress : String,
                             category : Int,
                             amount : Long,
                             vout : Int,
                             fee : Long,
                             confirmations : Int,
                             generated : Int,
                             blockHash : ByteArray,
                             blockIndex : Int,
                             blockTime : Int,
                             time : Int,
                             comment : String,
                             toComment : String) extends ProtocolMessage {
  override def toString() : String = {
    s"WalletTransactionDetail(account=${account}, address=${address}, toAddress=${toAddress}, category=${category}, amount=${amount}," +
      s"vout=${vout}, fee=${fee}, confirmations=${confirmations}, generated=${generated}, blockHash=${blockHash}, blockIndex=${blockIndex}," +
      s"time=${time}, blockTime=${blockTime}, comment=${comment}, toComment=${toComment})"
  }
}
