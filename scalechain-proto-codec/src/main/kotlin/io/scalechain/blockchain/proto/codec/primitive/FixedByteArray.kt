package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.util.ByteArray
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

object FixedByteArray {

  fun reverseCodec(length:Int) : Codec<ByteArray> {
    fun byteVectorToByteArray(byteVector : ByteVector) {
      byteVector.reverse.toArray
    }
    fun byteArrayToByteVector(byteArray : ByteArray) {
      ByteVector(byteArray.array.reverse)
    }

    bytes(length).xmap(
      byteVectorToByteArray _,
      byteArrayToByteVector _
    )
  }


  fun codec(length:Int) : Codec<ByteArray> {
    fun byteVectorToByteArray(byteVector : ByteVector) {
      byteVector.toArray
    }
    fun byteArrayToByteVector(byteArray : ByteArray) {
      ByteVector(byteArray.array)
    }

    bytes(length).xmap(
      byteVectorToByteArray _,
      byteArrayToByteVector _
    )
  }

  fun codec() : Codec<ByteArray> {
    fun byteVectorToByteArray(byteVector : ByteVector) {
      byteVector.toArray
    }
    fun byteArrayToByteVector(byteArray : ByteArray) {
      ByteVector(byteArray.array)
    }

    bytes.xmap(
      byteVectorToByteArray _,
      byteArrayToByteVector _
    )
  }


}
