package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import scala.language.implicitConversions

import scodec.Codec
import scodec.codecs.listOfN

object VarList {

  implicit fun varList<A>(codec: Codec<A>): Codec<List<A>> {
    listOfN(VarInt.countCodec, codec)
  }
}