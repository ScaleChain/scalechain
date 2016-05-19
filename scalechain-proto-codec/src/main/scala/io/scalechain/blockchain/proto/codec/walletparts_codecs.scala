package io.scalechain.blockchain.proto.codec

import java.math.BigInteger

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.{VarList, FixedByteArray}
import scodec.Codec
import scodec.codecs._



object AccountCodec extends MessagePartCodec[Account] {
  val codec : Codec[Account] = {
    // As the account is used as a key in KeyValueDatabase,
    // we should not use codecs such as utf8_32 which prefixes the encoded data with the length of the data.
    // If any length of data is encoded, we can't compare string values on a KeyValueDatabase.
    ("name" | utf8 )
  }.as[Account]
}


object OutPointCodec extends MessagePartCodec[OutPoint] {
  val codec : Codec[OutPoint] = {
    // Note that we are not using the reverseCodec here.
    // OutPointCodec is for writing keys and values on the wallet database, not for communicating with peers.
    ("transactionHash" | HashCodec.codec ) ::
    ("outputIndex" | int32L)
  }.as[OutPoint]
}

object WalletTransactionCodec extends MessagePartCodec[WalletTransaction] {
  val codec: Codec[WalletTransaction] = {
    ("blockHash"         | optional(bool(8), HashCodec.codec) ) ::
    ("blockIndex"        | optional(bool(8), int64) ) ::
    ("blockYime"         | optional(bool(8), int64) ) ::
    ("transactionId"     | optional(bool(8), HashCodec.codec) ) ::
    ("addedTime"         | int64 ) ::
    ("transactionIndex"  | optional(bool(8), int32) ) ::
    ("transaction"       | TransactionCodec.codec )
  }.as[WalletTransaction]
}

object OwnershipDescriptorCodec extends MessagePartCodec[OwnershipDescriptor] {
  val codec : Codec[OwnershipDescriptor] = {
    ("account"     | utf8_32) ::
    ("privateKeys" | VarList.varList(utf8) )
  }.as[OwnershipDescriptor]
}


object WalletOutputCodec extends MessagePartCodec[WalletOutput] {
  val codec : Codec[WalletOutput] = {
    ("blockindex"        | optional(bool(8), int64)) ::
    ("coinbase"          | bool ) ::
    ("spent"             | bool ) ::
    ("transactionOutput" | TransactionOutputCodec.codec )
  }.as[WalletOutput]
}
