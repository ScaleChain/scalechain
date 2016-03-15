package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import scala.language.implicitConversions

import scodec.{codecs, Codec}
import scodec.codecs.listOfN

object VarList {

  implicit def varList[A](codec: Codec[A]): Codec[List[A]] = {
    listOfN(VarInt.countCodec, codec)
  }

  /**
    * by mijeong
    *
    * list Prototype
    */
  implicit def list[A](codec: Codec[A]): Codec[List[A]] = {
    codecs.list(codec)
  }
}