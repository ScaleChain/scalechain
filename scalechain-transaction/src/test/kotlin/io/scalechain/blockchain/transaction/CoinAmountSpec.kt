package io.scalechain.blockchain.transaction

import org.scalatest._


/**
  * Created by kangmo on 5/18/16.
  */
class CoinAmountSpec extends FlatSpec with BeforeAndAfterEach with Matchers {
  "CoinAmount.from" should "create a CoinAmount with correct value" in {
    CoinAmount.from(100000000) shouldBe CoinAmount( scala.math.BigDecimal(1))
    CoinAmount.from(1) shouldBe CoinAmount( scala.math.BigDecimal(0.00000001))
  }
  "coinUnits" should "return a correct units of coins" in {
    CoinAmount( scala.math.BigDecimal(1)).coinUnits shouldBe 100000000
    CoinAmount( scala.math.BigDecimal(0.00000001)).coinUnits shouldBe 1
  }
}
