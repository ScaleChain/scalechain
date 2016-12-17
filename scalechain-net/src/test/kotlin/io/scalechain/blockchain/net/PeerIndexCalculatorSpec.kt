package io.scalechain.blockchain.net

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.GlobalEnvironemnt
import io.scalechain.util.PeerAddress
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class PeerIndexCalculatorSpec : FlatSpec(), Matchers {
  init {

    "getPeerIndexInternal" should "return None if address does not match" {
      val peers = listOf(
        PeerAddress("127.0.0.2", 1000),
        PeerAddress("127.0.0.2", 1001),
        PeerAddress("127.0.0.2", 1002)
      )

      PeerIndexCalculator.getPeerIndexInternal(1000, 0, peers) shouldBe null
      PeerIndexCalculator.getPeerIndexInternal(1001, 0, peers) shouldBe null
      PeerIndexCalculator.getPeerIndexInternal(1002, 0, peers) shouldBe null
    }

    "getPeerIndexInternal" should "return None if port does not match" {
      val peers = listOf(
        PeerAddress("127.0.0.1", 1000),
        PeerAddress("127.0.0.1", 1001),
        PeerAddress("127.0.0.1", 1002)
      )

      PeerIndexCalculator.getPeerIndexInternal(999,  0, peers) shouldBe null
      PeerIndexCalculator.getPeerIndexInternal(1003, 0, peers) shouldBe null
    }

    "getPeerIndexInternal" should "return Some(index) if port matches" {
      val peers = listOf(
        PeerAddress("127.0.0.1", 1000),
        PeerAddress("127.0.0.1", 1001),
        PeerAddress("127.0.0.1", 1002)
      )

      PeerIndexCalculator.getPeerIndexInternal(1000, 0, peers) shouldBe 0
      PeerIndexCalculator.getPeerIndexInternal(1001, 0, peers) shouldBe 1
      PeerIndexCalculator.getPeerIndexInternal(1002, 0, peers) shouldBe 2
    }



    /**
      * Assumption :
      * scalechain.conf has the following configuration.
      *
      *   p2p {
      *     port = 7643
      *     peers = [
      *       { address:"127.0.0.1", port:"7643" }, # index 0
      *       { address:"127.0.0.1", port:"7644" }, # index 1
      *       { address:"127.0.0.1", port:"7645" }, # index 2
      *       { address:"127.0.0.1", port:"7646" } # index 3
      *     ]
      *   }
      *
      * @return The peer index from [0, peer count-1]
      */
    "getPeerIndex" should "return None if port does not match" {
      // The unit test runs in scalechain/scalechain-net folder, but scalechain.conf file is in scalechain/config.
      // Need to override scalechain home where config folder exists.
      GlobalEnvironemnt.ScaleChainHome = "../"
      PeerIndexCalculator.getPeerIndex(7642) shouldBe null
      PeerIndexCalculator.getPeerIndex(7643) shouldBe 0
      PeerIndexCalculator.getPeerIndex(7644) shouldBe null
      /*
      PeerIndexCalculator.getPeerIndex(7644) shouldBe 1)
      PeerIndexCalculator.getPeerIndex(7645) shouldBe 2)
      PeerIndexCalculator.getPeerIndex(7646) shouldBe 3)
      PeerIndexCalculator.getPeerIndex(7647) shouldBe 4)
      PeerIndexCalculator.getPeerIndex(7648) shouldBe null
      */
    }
  }
}
