package io.scalechain.blockchain.proto.codec.primitive

/*
/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import io.scalechain.blockchain.proto.Ping
import io.scalechain.blockchain.proto.codec.CodecSuite
import io.scalechain.blockchain.proto.codec.PingCodec
import scodec.Codec

class VarListSpec : CodecSuite {

  import VarList.*

  "VarList codec" should {

    implicit val codec = varlistOf(PingCodec.codec)

    "roundtrip" {
      roundtrip(codec, listOf())
      roundtrip(listOf(
        Ping(BigInt(0)),
        Ping(BigInt(1)),
        Ping(BigInt(2))))
    }

  }
}
*/