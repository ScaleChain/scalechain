package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

class CStringSpec extends CodecSuite {

  import VarInt._

  "CString codec" should {

    implicit val countCodec = CString.codec

    "roundtrip" in {
      roundtrip("")
      roundtrip("A")
      roundtrip("AB")
    }
  }
}
