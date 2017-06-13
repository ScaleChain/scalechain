package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.script.hash
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class GenesisBlockSpec : FlatSpec(), Matchers {
  // BlockIndex is a trait. No need to create test cases for a trait.
  init {
    "Block" should "have a correct hash" {
      GenesisBlock.BLOCK.header.hash() shouldBe GenesisBlock.HASH
      GenesisBlock.SERIALIZED_GENESIS_BLOCK[0] shouldBe 1.toByte()
      GenesisBlock.SERIALIZED_GENESIS_BLOCK[1] shouldBe 0.toByte()
      GenesisBlock.SERIALIZED_GENESIS_BLOCK[2] shouldBe 0.toByte()
      GenesisBlock.SERIALIZED_GENESIS_BLOCK[3] shouldBe 0.toByte()
    }
  }
}