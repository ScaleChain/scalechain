package io.scalechain.blockchain.cli.network

import io.scalechain.blockchain.api.command.network.{GetPeerInfoResult, GetPeerInfo}
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class GetPeerInfoSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  "GetPeerInfo" should "should get the bitcoind as a peer." in {
    val response = invoke(GetPeerInfo)
    val result = response.right.get.get.asInstanceOf[GetPeerInfoResult]

    // We should have only one peer.
    result.peerInfos.size shouldBe 1
  }
}
