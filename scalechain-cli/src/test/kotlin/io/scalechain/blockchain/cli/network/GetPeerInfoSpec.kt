package io.scalechain.blockchain.cli.network

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.network.GetPeerInfoResult
import io.scalechain.blockchain.api.command.network.GetPeerInfo
import io.scalechain.blockchain.cli.APITestSuite
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */

// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class GetPeerInfoSpec : APITestSuite() {

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
    "GetPeerInfo" should "should get the bitcoind as a peer." {
      val response = invoke(GetPeerInfo)
      val result = response.right()!! as GetPeerInfoResult

      // We should have only one peer.
      result.peerInfos.size shouldBe 0
    }
  }
}
