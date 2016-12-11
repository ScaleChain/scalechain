package io.scalechain.blockchain.chain

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.transaction.TransactionTestDataTrait

class TransactionAnalyzerSpec : FlatSpec(), TransactionTestDataTrait, Matchers {
  val D = TransactionTestDataTrait
  init {
    "sumAmount" should "should sum the amount of transaction outputs" {
      TransactionAnalyzer.sumAmount(listOf(D.OUTPUT1, D.OUTPUT2, D.OUTPUT3)) shouldBe (D.OUTPUT1.value + D.OUTPUT2.value + D.OUTPUT3.value)
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