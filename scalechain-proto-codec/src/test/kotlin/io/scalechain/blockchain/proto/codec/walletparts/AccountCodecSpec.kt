package io.scalechain.blockchain.proto.codec.walletparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.CodecTestUtil

import io.scalechain.blockchain.proto.codec.AccountCodec
import org.junit.runner.RunWith

/**
  * Created by kangmo on 5/19/16.
  */
@RunWith(KTestJUnitRunner::class)
class AccountCodecSpec : FlatSpec(), Matchers, CodecTestUtil {

  val account1 = Account(
    name = "kangmo"
  )

  init {
    "AccountCodec" should "roundtrip" {
      roundTrip(AccountCodec, account1) shouldBe true
    }
  }
}

