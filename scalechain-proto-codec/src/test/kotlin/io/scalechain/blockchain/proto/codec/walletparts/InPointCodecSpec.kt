package io.scalechain.blockchain.proto.codec.walletparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.CodecTestUtil

import io.scalechain.blockchain.proto.codec.InPointCodec
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class InPointCodecSpec : FlatSpec(), Matchers, CodecTestUtil {

  val inPoint1 = InPoint(
    transactionHash  = Hash(bytes("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
    inputIndex       = 1
  )

  init {
    "InPointCodec" should "roundtrip" {
      roundTrip(InPointCodec, inPoint1) shouldBe true
    }
  }
}
