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
