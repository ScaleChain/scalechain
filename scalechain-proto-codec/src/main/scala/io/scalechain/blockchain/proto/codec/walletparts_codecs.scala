package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.{Hash, WalletTransactionAttribute, WalletTransaction, OutPoint}
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
    mappedEnum(uint32L,
      WalletTransactionAttribute.RECEIVING -> 1L,
      WalletTransactionAttribute.SPENDING -> 2L)
  }.as[WalletTransactionAttribute]

  val codec: Codec[WalletTransaction] = {
    ("outputOwnership" | utf8) ::
    ("attributes"      | VarList.varList(attributeCodec)) ::
    ("transactionHash" | HashCodec.codec)
  }.as[WalletTransaction]
}
