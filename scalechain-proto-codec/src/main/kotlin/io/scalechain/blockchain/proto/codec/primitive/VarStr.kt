package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import scala.language.implicitConversions

import scodec.Codec
import scodec.codecs.ascii
import scodec.codecs.variableSizeBytes

object VarStr {
  import VarInt._

  implicit val codec: Codec[String] = {
    variableSizeBytes(VarInt.countCodec, ascii)
  }
}