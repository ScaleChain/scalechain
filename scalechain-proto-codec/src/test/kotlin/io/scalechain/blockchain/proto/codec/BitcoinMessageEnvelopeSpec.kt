package io.scalechain.blockchain.proto.codec

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class BitcoinConfigurationSpec : FlatSpec(), Matchers {
  init {
    "magic of Bitcoin configuration" should "have the magic for the mainnet" {
      BitcoinConfiguration.config.magic shouldBe Magic.MAIN
    }
  }
}