package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import io.scalechain.util.UInt64

import scala.BigInt
import scala.math.BigInt.int2bigInt
import scala.math.BigInt.long2bigInt

import scodec.Codec
import scodec.codecs.int64L

object UInt64Codec {
  // BUGBUG : Make sure using int64L for UInt64L is safe.
  implicit val codec: Codec[UInt64] = int64L.xmap(UInt64.apply, _.value)

  implicit val bigIntCodec: Codec[BigInt] = Codec[UInt64].xmap(
    n => (UInt64.longToBigInt(n.value)),
    b => UInt64(UInt64.bigIntToLong(b)))
}
