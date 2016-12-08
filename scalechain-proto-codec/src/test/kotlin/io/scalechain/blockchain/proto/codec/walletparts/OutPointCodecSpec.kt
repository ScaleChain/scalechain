package io.scalechain.blockchain.proto.codec.walletparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.codec.OutPointCodec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.CodecTestUtil

import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OutPointCodecSpec : FlatSpec(), Matchers, CodecTestUtil {

  val outPoint1 = OutPoint(
    transactionHash  = Hash(bytes("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
    outputIndex      = 1
  )

  init {
    "OutPointCodec" should "roundtrip" {
      roundTrip(OutPointCodec, outPoint1) shouldBe true
    }
  }
}

