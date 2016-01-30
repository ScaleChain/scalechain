package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

class UInt64Spec extends CodecSuite {

  import UInt64._

  "UInt64 codec" should {
    "roundtrip" in {
      roundtrip(UInt64(1234))
      roundtrip(UInt64(12345))
      roundtrip(UInt64(Long.MinValue))
      roundtrip(UInt64(Long.MaxValue))
    }

    "print" in {
      def shouldPrint(n: BigInt) = {
        UInt64(UInt64.bigIntToLong(n)).toString shouldBe n.toString
      }

      shouldPrint(0)
      shouldPrint(1234)
      shouldPrint(Long.MaxValue)
      shouldPrint(BigInt(Long.MaxValue) * 2 + 1)
    }
  }
}
