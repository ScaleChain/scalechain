package io.scalechain.blockchain.cli.blockchain

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.blockchain.GetBlockCount
import io.scalechain.blockchain.api.domain.NumberResult
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.script.hash
import org.junit.Ignore
import org.junit.runner.RunWith

@Ignore
@RunWith(KTestJUnitRunner::class)
class GetBlockCountSpec : APITestSuite() {
  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  init {
    "GetBlockCount" should "return the number of blocks in the local best block chain" {
      val zero = (invoke(GetBlockCount).right() as NumberResult).value.toInt()
      zero shouldBe 0

      chain.putBlock(chain.db, Data.Block.BLK01.header.hash(), Data.Block.BLK01)
      val one = (invoke(GetBlockCount).right() as NumberResult).value.toInt()
      one shouldBe 1

      chain.putBlock(chain.db, Data.Block.BLK02.header.hash(), Data.Block.BLK02)
      val two = (invoke(GetBlockCount).right() as NumberResult).value.toInt()
      two shouldBe 2
    }
  }
}
