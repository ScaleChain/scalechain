package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.WalletTransaction
import scodec.Codec
import scodec.codecs.{bool, int32, int64, optional}

/**
  * Created by shannon on 16. 12. 27.
  */
//
//object AssetDefinitionCodec extends MessagePartCodec[WalletTransaction] {
//  val codec: Codec[WalletTransaction] = {
//    ("blockHash"         | optional(bool(8), HashCodec.codec) ) ::
//      ("blockIndex"        | optional(bool(8), int64) ) ::
//      ("blockTime"         | optional(bool(8), int64) ) ::
//      ("transactionId"     | optional(bool(8), HashCodec.codec) ) ::
//      ("addedTime"         | int64 ) ::
//      ("transactionIndex"  | optional(bool(8), int32) ) ::
//      ("transaction"       | TransactionCodec.codec )
//  }.as[WalletTransaction]
//}
//
