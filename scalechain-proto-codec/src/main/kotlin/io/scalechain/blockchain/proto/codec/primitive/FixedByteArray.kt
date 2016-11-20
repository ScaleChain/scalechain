package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.util.ByteArray
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

object FixedByteArray {

  def reverseCodec(length:Int) : Codec[ByteArray] = {
    def byteVectorToByteArray(byteVector : ByteVector) = {
      byteVector.reverse.toArray
    }
    def byteArrayToByteVector(byteArray : ByteArray) = {
      ByteVector(byteArray.array.reverse)
    }

    bytes(length).xmap(
      byteVectorToByteArray _,
      byteArrayToByteVector _
    )
  }


  def codec(length:Int) : Codec[ByteArray] = {
    def byteVectorToByteArray(byteVector : ByteVector) = {
      byteVector.toArray
    }
    def byteArrayToByteVector(byteArray : ByteArray) = {
      ByteVector(byteArray.array)
    }

    bytes(length).xmap(
      byteVectorToByteArray _,
      byteArrayToByteVector _
    )
  }

  def codec() : Codec[ByteArray] = {
    def byteVectorToByteArray(byteVector : ByteVector) = {
      byteVector.toArray
    }
    def byteArrayToByteVector(byteArray : ByteArray) = {
      ByteVector(byteArray.array)
    }

    bytes.xmap(
      byteVectorToByteArray _,
      byteArrayToByteVector _
    )
  }


}
