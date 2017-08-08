package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith


/**
  * Created by kangmo on 5/18/16.
  */
@RunWith(KTestJUnitRunner::class)
class CoinAmountSpec : FlatSpec(), Matchers {
  init {
    "CoinAmount.from" should "create a CoinAmount with correct value" {
      CoinAmount.from(100000000L) shouldBe CoinAmount( java.math.BigDecimal("1"))
      CoinAmount.from(1L) shouldBe CoinAmount( java.math.BigDecimal("0.00000001"))
    }
    "coinUnits" should "return a correct units of coins" {
      CoinAmount( java.math.BigDecimal("1")).coinUnits() shouldBe 100000000L
      CoinAmount( java.math.BigDecimal("0.00000001")).coinUnits() shouldBe 1L
    }
  }
}
