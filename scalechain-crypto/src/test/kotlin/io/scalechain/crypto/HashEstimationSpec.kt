package io.scalechain.crypto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class HashEstimationSpec : FlatSpec(), Matchers {

  init {
    "getHashCalculations" should "calculate the number of hash calculations based on a hash value" {
      HashEstimation.getHashCalculations(bytes("F000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1L
      HashEstimation.getHashCalculations(bytes("E000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1L
      HashEstimation.getHashCalculations(bytes("9000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1L
      HashEstimation.getHashCalculations(bytes("8000000000000000000000000000000000000000000000000000000000000000")) shouldBe 1L
      HashEstimation.getHashCalculations(bytes("7000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2L
      HashEstimation.getHashCalculations(bytes("5000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2L
      HashEstimation.getHashCalculations(bytes("4000000000000000000000000000000000000000000000000000000000000000")) shouldBe 2L
      HashEstimation.getHashCalculations(bytes("3000000000000000000000000000000000000000000000000000000000000000")) shouldBe 4L
      HashEstimation.getHashCalculations(bytes("2000000000000000000000000000000000000000000000000000000000000000")) shouldBe 4L
      HashEstimation.getHashCalculations(bytes("1000000000000000000000000000000000000000000000000000000000000000")) shouldBe 8L
      HashEstimation.getHashCalculations(bytes("0F00000000000000000000000000000000000000000000000000000000000000")) shouldBe 16L
      HashEstimation.getHashCalculations(bytes("0E00000000000000000000000000000000000000000000000000000000000000")) shouldBe 16L
      HashEstimation.getHashCalculations(bytes("0900000000000000000000000000000000000000000000000000000000000000")) shouldBe 16L
      HashEstimation.getHashCalculations(bytes("0800000000000000000000000000000000000000000000000000000000000000")) shouldBe 16L
      HashEstimation.getHashCalculations(bytes("0700000000000000000000000000000000000000000000000000000000000000")) shouldBe 32L
    }
  }
}