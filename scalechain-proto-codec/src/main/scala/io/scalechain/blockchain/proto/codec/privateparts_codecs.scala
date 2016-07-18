package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.{FixedByteArray, Bool, VarStr, BigIntForLongCodec}
import scodec.Codec
import scodec.codecs._

object BlockConsensusCodec extends MessagePartCodec[BlockConsensus] {
  val codec : Codec[BlockConsensus] = {
    ("header" | BlockHeaderCodec.codec) ::
    ("height" | int64L )
  }.as[BlockConsensus]
}
