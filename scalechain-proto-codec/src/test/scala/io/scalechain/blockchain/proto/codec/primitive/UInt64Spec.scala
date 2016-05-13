package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

class UInt64Spec extends CodecSuite {

  implicit val int64codec = BigIntForLongCodec.int64codec

  "UInt64 codec" should {
    "roundtrip" in {
      roundtrip(UInt64(1234))
      roundtrip(UInt64(12345))
      roundtrip(UInt64(Long.MinValue))
      roundtrip(UInt64(Long.MaxValue))
    }

    "print" in {
      def shouldPrint(n: BigInt) = {
        UInt64(UInt64.bigIntToLong(n)).toString shouldBe s"UInt64(${n.toString}L)"
      }

      shouldPrint(0)
      shouldPrint(1234)
      shouldPrint(Long.MaxValue)
      // BUGBUG : Need to pass this test?
      //shouldPrint(BigInt(Long.MaxValue) * 2 + 1)
    }
  }
}
