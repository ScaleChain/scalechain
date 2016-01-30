package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

class VarStrSpec extends CodecSuite {

  import VarStr._

  "VarStr codec" should {

    "roundtrip" in {
      roundtrip("Hello")
      roundtrip("")
    }

  }
}
