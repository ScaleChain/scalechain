package io.scalechain.blockchain.proto.codec.primitive

import scodec.Codec
import scodec.codecs._

/**
  * Created by kangmo on 2/2/16.
  */
object Bool {
  val codec : Codec[Boolean] = {
    mappedEnum(uint8L, false->0, true->1)
  }.as[Boolean]
}
