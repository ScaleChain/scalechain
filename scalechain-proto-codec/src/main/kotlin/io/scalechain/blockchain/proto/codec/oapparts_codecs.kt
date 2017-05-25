package io.scalechain.blockchain.proto.codec


import io.scalechain.blockchain.proto.FixedByteArrayMessage
import io.scalechain.util.HexUtil
import io.scalechain.blockchain.proto.StringMessage
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import io.scalechain.util.Bytes
import io.scalechain.blockchain.proto.codec.primitive.CHexByteArrayCodec
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


// TODO : Add a test case
object FixedByteArrayMessageCodec : Codec<FixedByteArrayMessage> {
  override fun transcode(io : CodecInputOutputStream, obj : FixedByteArrayMessage? ) : FixedByteArrayMessage? {
    val bytes = CHexByteArrayCodec().transcode(io, obj?.value?.array);
    //val bytes = Codecs.CHexByteArray.transcode(io, obj?.value?.array);
    if (io.isInput) {
      return FixedByteArrayMessage(Bytes(bytes!!))
    }
    return null
  }
}



// TODO : Add a test case
object StringMessageCodec : Codec<StringMessage> {
  override fun transcode(io: CodecInputOutputStream, obj: StringMessage?): StringMessage? {
    val value = Codecs.CString.transcode(io, obj?.value)
    if (io.isInput) {
      return StringMessage(
        value!!
      )
    }
    return null
  }
}

