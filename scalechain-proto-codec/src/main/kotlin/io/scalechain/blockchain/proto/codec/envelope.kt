package io.scalechain.blockchain.proto.codec

import io.scalechain.util.Bytes
import io.scalechain.blockchain.proto.Checksum
import io.scalechain.blockchain.proto.Magic
import io.scalechain.blockchain.proto.codec.primitive.Codecs

/**
 * Created by kangmo on 16/01/2017.
 */
object ChecksumCodec : Codec<Checksum> {
  override fun transcode(io: CodecInputOutputStream, obj: Checksum?): Checksum? {
    val value = Codecs.fixedByteArray(Checksum.VALUE_SIZE).transcode(io, obj?.value?.array)
    if (io.isInput) {
      return Checksum(Bytes(value!!))
    }

    return null
  }
}

object MagicCodec : Codec<Magic> {
  override fun transcode(io: CodecInputOutputStream, obj: Magic?): Magic? {
    val value = Codecs.fixedReversedByteArray(Magic.VALUE_SIZE).transcode(io, obj?.value?.array)
    if (io.isInput) {
      return Magic(Bytes(value!!))
    }

    return null
  }
}