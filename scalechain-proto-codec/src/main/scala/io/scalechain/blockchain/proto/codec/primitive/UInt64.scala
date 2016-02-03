package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */


/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */
case class UInt64(value: Long) {
  //override def toString = s"UInt64(${UInt64.longToBigInt(value).toString})"
  override def toString = s"UInt64(${value}L)"
}

object UInt64 {

  def longToBigInt(unsignedLong: Long): BigInt =
    (BigInt(unsignedLong >>> 1) << 1) + (unsignedLong & 1)

  def bigIntToLong(n: BigInt): Long = {
    val smallestBit = (n & 1).toLong
    ((n >> 1).toLong << 1) | smallestBit
  }
}

import scala.BigInt
import scala.math.BigInt.int2bigInt
import scala.math.BigInt.long2bigInt

import scodec.Codec
import scodec.codecs.int64L

object BigIntCodec {
  // BUGBUG : Make sure using int64L for UInt64L is safe.
  val int64codec: Codec[UInt64] = int64L.xmap(UInt64.apply, _.value)

  implicit val codec: Codec[BigInt] = int64codec.xmap(
    n => (UInt64.longToBigInt(n.value)),
    b => UInt64(UInt64.bigIntToLong(b)))
}
