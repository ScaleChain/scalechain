package io.scalechain.blockchain.proto.codec

import java.math.BigInteger

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.{VarList, FixedByteArray}
import scodec.Codec
import scodec.codecs._

/** The codec for OutPoint.
  */
object OutPointCodec extends MessagePartCodec[OutPoint] {
  val codec : Codec[OutPoint] = {
    // Note that we are not using the reverseCodec here.
    // OutPointCodec is for writing keys and values on the wallet database, not for communicating with peers.
    ("transactionHash" | HashCodec.codec ) ::
    ("outputIndex" | int32L)
  }.as[OutPoint]
}

object WalletTransactionCodec extends MessagePartCodec[WalletTransaction] {

  import WalletTransactionAttribute._

  val attributeCodec: Codec[WalletTransactionAttribute] = {
    mappedEnum(int32,
      WalletTransactionAttribute.SEND -> 1,
      WalletTransactionAttribute.RECEIVE -> 2,
      WalletTransactionAttribute.GENERATE -> 3,
      WalletTransactionAttribute.IMMATURE -> 4,
      WalletTransactionAttribute.ORPHAN -> 5,
      WalletTransactionAttribute.MOVE -> 6
    )
  }.as[WalletTransactionAttribute]

//  address          = Some("n3GNqMveyvaPvUbH469vDRadqpJMPc84JA"),

  val codec: Codec[WalletTransaction] = {
    ("involvesWatchonly" | bool ) ::
    ("account"           | utf8_32 ) ::
    ("outputOwnership"   | optional(bool(8), utf8_32)) ::
    ("attributes"        | attributeCodec ) ::
    ("amount"            | primitive.BigDecimal.codec) ::
    ("fee"               | optional(bool(8), primitive.BigDecimal.codec) ) ::
    ("confirmations"     | optional(bool(8), int64) ) ::
    ("generated"         | optional(bool(8), bool) ) ::
    ("blockhash"         | optional(bool(8), HashCodec.codec) ) ::
    ("blockindex"        | optional(bool(8), int64) ) ::
    ("blocktime"         | optional(bool(8), int64) ) ::
    ("txid"              | optional(bool(8), HashCodec.codec) ) ::
    ("time"              | int64 ) ::
    ("timereceived"      | optional(bool(8), int64) ) ::
    ("transaction"       | TransactionCodec.codec )
  }.as[WalletTransaction]
}

object OwnershipDescriptorCodec extends MessagePartCodec[OwnershipDescriptor] {
  val codec : Codec[OwnershipDescriptor] = {
    ("privateKeys" | VarList.varList(utf8) )
  }.as[OwnershipDescriptor]
}


object WalletOutputCodec extends MessagePartCodec[WalletOutput] {
  val codec : Codec[WalletOutput] = {
    ("spent"             | bool ) ::
    ("outPoint"          | OutPointCodec.codec ) ::
    ("address"           | optional(bool(8), utf8_32)) ::
    ("account"           | optional(bool(8), utf8_32)) ::
    ("lockingScript"     | LockingScriptCodec.codec ) ::
    ("redeemScript"      | optional(bool(8), utf8_32)) ::
    ("amount"            | primitive.BigDecimal.codec ) ::
    ("confirmations"     | int64 ) ::
    ("involvesWatchonly" | bool )
  }.as[WalletOutput]
}



