package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.*

object BlockConsensusCodec : Codec<BlockConsensus> {
  override fun transcode(io : CodecInputOutputStream, obj : BlockConsensus? ) : BlockConsensus? {
    val header = BlockHeaderCodec.transcode(io, obj?.header)
    val height = Codecs.Int64L.transcode(io, obj?.height)

    if (io.isInput) {
      return BlockConsensus(
        header!!,
        height!!
      )
    }
    return null
  }
}
