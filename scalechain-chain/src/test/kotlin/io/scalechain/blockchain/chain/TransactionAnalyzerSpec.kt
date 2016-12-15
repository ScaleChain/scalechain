package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.transaction.TransactionTestData
import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(KTestJUnitRunner::class)
class TransactionAnalyzerSpec : FlatSpec(), TransactionTestInterface, Matchers {
  val D = TransactionTestData
  init {
    "sumAmount" should "should sum the amount of transaction outputs" {
      TransactionAnalyzer.sumAmount(listOf(D.OUTPUT1, D.OUTPUT2, D.OUTPUT3)) shouldBe BigDecimal(D.OUTPUT1.value + D.OUTPUT2.value + D.OUTPUT3.value)
    }

    /*
    "calculateFee" should "" {
      // TODO : Implement
    }

    "getSpentOutputs" should "" {
      // TODO : Implement
    }
    */
  }
}