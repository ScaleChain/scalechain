package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.Codecs

/**
  * Codec for encoding and decoding OapOutputCacheItem.
  *
  */
/*
object OutputCacheItemCodec extends MessagePartCodec[OutputCacheItem] {
  override val codec: Codec[OutputCacheItem] = {
    ( "output"   | TransactionOutputCodec.codec ) ::
    ( "assetId"  | CString.codec ) ::
    ( "quantity" | int32L )
  }.as[OutputCacheItem]
}
*/

// TODO : Move to proto-codec, add a test case.
object OutputCacheItemCodec : Codec<OutputCacheItem> {
  override fun transcode(io: CodecInputOutputStream, obj: OutputCacheItem?): OutputCacheItem? {
    val output        = TransactionOutputCodec.transcode(io, obj?.output)
    val assetId       = Codecs.CString.transcode(io, obj?.assetId)
    // TODO : Use Int32 instead of Int32L?
    val quantity      = Codecs.Int32L.transcode(io, obj?.quantity)
    if (io.isInput) {
      return OutputCacheItem(
        output!!,
        assetId!!,
        quantity!!
      )
    }
    return null
  }
}
