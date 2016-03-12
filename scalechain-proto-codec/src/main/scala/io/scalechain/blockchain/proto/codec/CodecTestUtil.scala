package io.scalechain.blockchain.proto.codec

import scodec.Codec
import scodec.bits.BitVector


trait CodecTestUtil {
  def decodeFully[T](bitVector:BitVector)(implicit codec:Codec[T]) : T =
    codec.decode(bitVector).require.value

  def encode[T](message : T)(implicit codec:Codec[T]) =
    codec.encode(message).require
}

