package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class BlockOrphanageSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockOrphangeSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  var o: BlockOrphanage = null

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()

    o = chain.blockOrphanage
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "hasOrphan" should "return false for a non-existent orphan" in {
    o.hasOrphan(BLK02.header.hash) shouldBe false
  }

  "hasOrphan" should "return true for a orphan block" in {
    o.putOrphan(BLK02)
    o.hasOrphan(BLK02.header.hash) shouldBe true
  }

  "putOrphan" should "be able to put child orphans first" in {
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.putOrphan(BLK02)

    o.hasOrphan(BLK02.header.hash) shouldBe true
    o.hasOrphan(BLK03a.header.hash) shouldBe true
    o.hasOrphan(BLK03b.header.hash) shouldBe true
    o.getOrphansDependingOn(BLK03a.header.hash) shouldBe List()
    o.getOrphansDependingOn(BLK03b.header.hash) shouldBe List()
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }

  "putOrphan" should "be able to put parent orphans first" in {
    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)

    o.hasOrphan(BLK02.header.hash) shouldBe true
    o.hasOrphan(BLK03a.header.hash) shouldBe true
    o.hasOrphan(BLK03b.header.hash) shouldBe true
    o.getOrphansDependingOn(BLK03a.header.hash) shouldBe List()
    o.getOrphansDependingOn(BLK03b.header.hash) shouldBe List()
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }

  "putOrphan" should "be able to put orphans in mixed order" in {
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.putOrphan(BLK02)

    o.hasOrphan(BLK02.header.hash) shouldBe true
    o.hasOrphan(BLK03a.header.hash) shouldBe true
    o.hasOrphan(BLK03b.header.hash) shouldBe true
    o.getOrphansDependingOn(BLK03a.header.hash) shouldBe List()
    o.getOrphansDependingOn(BLK03b.header.hash) shouldBe List()
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }


  "getOrphan" should "return None for a non-existent orphan" in {
    o.getOrphan(BLK02.header.hash) shouldBe None
  }


  "getOrphan" should "return an orphan" in {
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.putOrphan(BLK02)

    o.getOrphan(BLK03a.header.hash) shouldBe Some(BLK03a)
    o.getOrphan(BLK03b.header.hash) shouldBe Some(BLK03b)
    o.getOrphan(BLK02.header.hash) shouldBe Some(BLK02)

  }


  "delOrphan" should "delete an orphan" in {
    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)

    o.delOrphan(BLK02)
    o.hasOrphan(BLK02.header.hash) shouldBe false

    o.delOrphan(BLK03b)
    o.hasOrphan(BLK03b.header.hash) shouldBe false

    // The BLK03a was not deleted.
    o.hasOrphan(BLK03a.header.hash) shouldBe true

  }

  "getOrphanRoot" should "return itself if the parent of it is missing" in {
    o.putOrphan(BLK02)

    o.getOrphanRoot(BLK02.header.hash) shouldBe BLK02.header.hash
  }

  "getOrphanRoot" should "return an orphan which misses a parent" in {
    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)

    o.getOrphanRoot(BLK03a.header.hash) shouldBe BLK02.header.hash
  }


  "getOrphanRoot" should "return an orphan which misses a parent, and even though it has a child" in {
    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)
    o.putOrphan(BLK04a)

    o.getOrphanRoot(BLK03a.header.hash) shouldBe BLK02.header.hash
  }


  "getOrphansDependingOn" should "return depending orphans if even though the parent was not put yet" in {
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }

  "removeDependenciesOn" should "remove dependent blocks for a block" in {
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)

    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)

    o.removeDependenciesOn(BLK02.header.hash)

    o.getOrphansDependingOn(BLK02.header.hash).toList shouldBe List()
  }
}
