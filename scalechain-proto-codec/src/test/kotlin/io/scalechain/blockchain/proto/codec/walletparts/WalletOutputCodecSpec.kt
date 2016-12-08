package io.scalechain.blockchain.proto.codec.walletparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.codec.WalletOutputCodec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.proto.test.ProtoTestData
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class WalletOutputCodecSpec : FlatSpec(), Matchers, CodecTestUtil, ProtoTestData {

  val walletOutput1 =
      WalletOutput(
          blockindex = 100L,
          coinbase = true,
          spent = false,
          transactionOutput = transaction1().outputs[0]
      )

  init {
    "WalletOutputCodec" should "roundtrip" {
      roundTrip(WalletOutputCodec, walletOutput1) shouldBe true
    }

    "WalletOutputCodec" should "roundtrip with parse/serialize" {
      val serialized = WalletOutputCodec.encode(walletOutput1)
      val parsed: WalletOutput = WalletOutputCodec.decode(serialized)!!
      parsed shouldBe walletOutput1
    }
  }
}



