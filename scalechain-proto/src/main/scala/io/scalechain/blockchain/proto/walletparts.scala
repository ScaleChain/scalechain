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
                   accountNum: Int,
                   account: String,
                   addresses : List[Address]) extends ProtocolMessage {


  override def toString() : String = {
    s"Account(header=${header}, accountNum:${accountNum}, account=${account}, addresses=List(${addresses.mkString(",")}))"
  }
}

case class AccountHeader(version : Int,
                         timestamp : Long)  extends ProtocolMessage {

  override def toString() : String = {
    s"AccountHeader(version=${version}, timestamp=${timestamp}L)"
  }
}

case class Address(addressNum : Int,
                   address : String,
                   publicKey: Hash,
                   privateKey: Hash,
                   purpose: Int)  extends ProtocolMessage {

  override def toString() : String = {
    s"Address(addressNum=$addressNum, address=${address}, publicKey=${publicKey}, privateKey=${privateKey}, purpose=${purpose})"
  }
}
