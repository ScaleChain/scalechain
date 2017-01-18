package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.CString
import scodec.Codec
import scodec.codecs._

/**
  * Codec for encoding and decoding OapOutputCacheItem.
  *
  */
object OutputCacheItemCodec extends MessagePartCodec[OutputCacheItem] {
  override val codec: Codec[OutputCacheItem] = {
    ( "output"   | TransactionOutputCodec.codec ) ::
    ( "assetId"  | CString.codec ) ::
    ( "quantity" | int32L )
  }.as[OutputCacheItem]
}

