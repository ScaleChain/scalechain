package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.util.ByteArray
import scodec.Codec
import scodec.codecs._
import io.scalechain.util.ByteArrayAndVectorConverter._

/**
  * Created by kangmo on 1/31/16.
  */
object VarByteArray {
/*
  def byteArrayToVectorWithReverse(barray : ByteArray   ) = barray.array.reverse.toVector
  def vectorToByteArrayWithReverse(vector : Vector[Byte]) = ByteArray(vector.toArray[Byte].reverse)

  val reversingCodec : Codec[ByteArray] =
    vectorOfN(VarInt.countCodec, byte).xmap(
      vectorToByteArrayWithReverse,
      byteArrayToVectorWithReverse)
*/
  val codec : Codec[ByteArray] =
    vectorOfN(VarInt.countCodec, byte).xmap(
      vectorToByteArray,
      byteArrayToVector)

}
