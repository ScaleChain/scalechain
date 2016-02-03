package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import scala.BigInt

import BigIntCodec.codec

import scodec.Attempt.{ Failure, Successful }
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.uint16L
import scodec.codecs.uint32L
import scodec.codecs.uint8L

case class VarInt(value: Long)

object VarInt {

  import scodec.codecs._

  implicit val varIntCodec = Codec[Long](
    (n: Long) =>
      n match {
        case i if (i < 0xfd) =>
          uint8L.encode(i.toInt)
        case i if (i < 0xffff) =>
          for {
            a <- uint8L.encode(0xfd)
            b <- uint16L.encode(i.toInt)
          } yield a ++ b
        case i if (i < 0xffffffffL) =>
          for {
            a <- uint8L.encode(0xfe)
            b <- uint32L.encode(i)
          } yield a ++ b
        case i =>
          for {
            a <- uint8L.encode(0xff)
            b <- Codec[BigInt].encode(BigInt(i))
          } yield a ++ b
      },
    (buf: BitVector) => {
      uint8L.decode(buf) match {
        case Successful(byte) =>
          byte.value match {
            case 0xff =>
              Codec[BigInt].decode(byte.remainder)
                .map { case b => b.map(_.toLong) }
            case 0xfe =>
              uint32L.decode(byte.remainder)
            case 0xfd =>
              uint16L.decode(byte.remainder)
                .map { case b => b.map(_.toLong) }
            case _ =>
              Successful(scodec.DecodeResult(byte.value.toLong, byte.remainder))
          }
        case Failure(err) =>
          Failure(err)
      }
    })

  // The codec for getting the number of variable items to construct a new codec with listOfN or variableSizeBytes.
  // TODO : Add a unit test.
  val countCodec : Codec[Int] = VarInt.varIntCodec.xmap((_:Long).toInt, (_:Int).toLong)
}
