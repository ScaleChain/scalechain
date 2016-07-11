package io.scalechain.blockchain.cli

/**
  * Created by kangmo on 5/22/16.
  */

import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.{PeerAddress, HexUtil}
import HexUtil._

import org.scalatest._

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
class CoinMinerSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>
  import Hash._

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

  "isLessThan" should "return true only if the left hash is less than the right hash." in {
    import CoinMiner._
    isLessThan(Hash("00000"), Hash("00000")) shouldBe false
    isLessThan(Hash("00001"), Hash("00000")) shouldBe false
    isLessThan(Hash("00000"), Hash("00001")) shouldBe true
    isLessThan(Hash("0000F"), Hash("0000E")) shouldBe false
    isLessThan(Hash("0000E"), Hash("0000F")) shouldBe true

    isLessThan(Hash("10000"), Hash("00000")) shouldBe false
    isLessThan(Hash("00000"), Hash("10000")) shouldBe true
    isLessThan(Hash("F0000"), Hash("E0000")) shouldBe false
    isLessThan(Hash("E0000"), Hash("F0000")) shouldBe true


    isLessThan(Hash("FFFFF"), Hash("FFFFF")) shouldBe false
    isLessThan(Hash("FFFFF"), Hash("FFFFE")) shouldBe false
    isLessThan(Hash("FFFFE"), Hash("FFFFF")) shouldBe true
    isLessThan(Hash("FFFFF"), Hash("EFFFF")) shouldBe false
    isLessThan(Hash("EFFFF"), Hash("FFFFF")) shouldBe true
  }


  "getPeerIndexInternal" should "return None if address does not match" in {
    val peers = List(
      PeerAddress("127.0.0.2", 1000),
      PeerAddress("127.0.0.2", 1001),
      PeerAddress("127.0.0.2", 1002)
    )

    CoinMiner.getPeerIndexInternal(1000, 0, peers) shouldBe None
    CoinMiner.getPeerIndexInternal(1001, 0, peers) shouldBe None
    CoinMiner.getPeerIndexInternal(1002, 0, peers) shouldBe None
  }

  "getPeerIndexInternal" should "return None if port does not match" in {
    val peers = List(
      PeerAddress("127.0.0.1", 1000),
      PeerAddress("127.0.0.1", 1001),
      PeerAddress("127.0.0.1", 1002)
    )

    CoinMiner.getPeerIndexInternal(999,  0, peers) shouldBe None
    CoinMiner.getPeerIndexInternal(1003, 0, peers) shouldBe None
  }

  "getPeerIndexInternal" should "return Some(index) if port matches" in {
    val peers = List(
      PeerAddress("127.0.0.1", 1000),
      PeerAddress("127.0.0.1", 1001),
      PeerAddress("127.0.0.1", 1002)
    )

    CoinMiner.getPeerIndexInternal(1000, 0, peers) shouldBe Some(0)
    CoinMiner.getPeerIndexInternal(1001, 0, peers) shouldBe Some(1)
    CoinMiner.getPeerIndexInternal(1002, 0, peers) shouldBe Some(2)
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
    *       { address:"127.0.0.1", port:"7646" }, # index 3
    *       { address:"127.0.0.1", port:"7647" }  # index 4
    *     ]
    *   }
    *
    * @return The peer index from [0, peer count-1)
    */
  "getPeerIndex" should "return None if port does not match" in {
    CoinMiner.getPeerIndex(7642) shouldBe None
    CoinMiner.getPeerIndex(7643) shouldBe Some(0)
    CoinMiner.getPeerIndex(7644) shouldBe Some(1)
    CoinMiner.getPeerIndex(7645) shouldBe Some(2)
    CoinMiner.getPeerIndex(7646) shouldBe Some(3)
    CoinMiner.getPeerIndex(7647) shouldBe Some(4)
    CoinMiner.getPeerIndex(7648) shouldBe None
  }
}

