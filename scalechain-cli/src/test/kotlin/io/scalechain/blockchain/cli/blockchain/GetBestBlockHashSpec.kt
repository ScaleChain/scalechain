package io.scalechain.blockchain.cli.blockchain

import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite

/**
  * Created by kangmo on 11/2/15.
  */
class GetBestBlockHashSpec : APITestSuite() {
  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  init {
    "GetBestBlockHash" should "return the best block hash" {
      val result = invoke(GetBestBlockHash)
      (result.right()!! as StringResult).value.length shouldBe 64
    }
  }
}
