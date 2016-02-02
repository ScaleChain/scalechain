package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import io.scalechain.blockchain.proto.Ping
import io.scalechain.blockchain.proto.codec.PingCodec
import io.scalechain.util.UInt64
import scodec.Codec

class VarListSpec extends CodecSuite {

  import VarList._

  "VarList codec" should {

    implicit val codec = varList(PingCodec.codec)

    "roundtrip" in {
      roundtrip(codec, List())
      roundtrip(List(
        Ping(UInt64(0)),
        Ping(UInt64(1)),
        Ping(UInt64(2))))
    }

  }
}
