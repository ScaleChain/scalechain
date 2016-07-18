package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Hash._
import io.scalechain.util.HexUtil
import io.scalechain.util.PeerAddress
import io.scalechain.util.{PeerAddress, HexUtil}
import HexUtil._

import org.scalatest._

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
class BlockGatewaySpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
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

  "getPeerIndexInternal" should "return None if address does not match" in {
    val peers = List(
      PeerAddress("127.0.0.2", 1000),
      PeerAddress("127.0.0.2", 1001),
      PeerAddress("127.0.0.2", 1002)
    )

    BlockGateway.getPeerIndexInternal(1000, 0, peers) shouldBe None
    BlockGateway.getPeerIndexInternal(1001, 0, peers) shouldBe None
    BlockGateway.getPeerIndexInternal(1002, 0, peers) shouldBe None
  }

  "getPeerIndexInternal" should "return None if port does not match" in {
    val peers = List(
      PeerAddress("127.0.0.1", 1000),
      PeerAddress("127.0.0.1", 1001),
      PeerAddress("127.0.0.1", 1002)
    )

    BlockGateway.getPeerIndexInternal(999,  0, peers) shouldBe None
    BlockGateway.getPeerIndexInternal(1003, 0, peers) shouldBe None
  }

  "getPeerIndexInternal" should "return Some(index) if port matches" in {
    val peers = List(
      PeerAddress("127.0.0.1", 1000),
      PeerAddress("127.0.0.1", 1001),
      PeerAddress("127.0.0.1", 1002)
    )

    BlockGateway.getPeerIndexInternal(1000, 0, peers) shouldBe Some(0)
    BlockGateway.getPeerIndexInternal(1001, 0, peers) shouldBe Some(1)
    BlockGateway.getPeerIndexInternal(1002, 0, peers) shouldBe Some(2)
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
    * @return The peer index from [0, peer count-1)
    */
  "getPeerIndex" should "return None if port does not match" in {
    BlockGateway.getPeerIndex(7642) shouldBe None
    BlockGateway.getPeerIndex(7643) shouldBe Some(0)
    BlockGateway.getPeerIndex(7644) shouldBe Some(1)
    BlockGateway.getPeerIndex(7645) shouldBe Some(2)
    BlockGateway.getPeerIndex(7646) shouldBe Some(3)
    BlockGateway.getPeerIndex(7647) shouldBe None
  }
}

