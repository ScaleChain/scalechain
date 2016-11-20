package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{TransactionSampleData, BlockSampleData, BlockchainTestTrait}
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

class BlockProcessorSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-BlockProcessorSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  var b : BlockProcessor = null

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()
    keyValueDB = db

    b = BlockProcessor(chain)
    // Put the genesis block for testing.
    b.acceptBlock(env.GenesisBlockHash, env.GenesisBlock)

  }

  override fun afterEach() {
    super.afterEach()

    keyValueDB = null
    b = null
    // finalize a test.
  }

  "getBlock" should "return Some(block) for a non-orphan block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.getBlock(BLK01.header.hash) shouldBe Some(BLK01)

    b.acceptBlock(BLK02.header.hash, BLK02)
    b.getBlock(BLK02.header.hash) shouldBe Some(BLK02)
  }

  "getBlock" should "return None for a non-existent block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.getBlock(BLK01.header.hash) shouldBe None
    b.getBlock(BLK02.header.hash) shouldBe None
  }

  "getBlock" should "return None for an orphan block block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)
    b.getBlock(BLK02.header.hash) shouldBe None
    b.getBlock(BLK03a.header.hash) shouldBe None
  }

  "exists" should "return false for a non-existent block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.exists(BLK01.header.hash) shouldBe false
    b.exists(BLK02.header.hash) shouldBe false
  }

  "exists" should "return true for a block on the blockchain" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.exists(BLK01.header.hash) shouldBe true

    b.acceptBlock(BLK02.header.hash, BLK02)
    b.exists(BLK02.header.hash) shouldBe true
  }

  "exists" should "return true for an orphan block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)
    b.exists(BLK02.header.hash) shouldBe true
    b.exists(BLK03a.header.hash) shouldBe true
  }


  "hasOrphan" should "return false for a non-existent block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.hasOrphan(BLK01.header.hash) shouldBe false
    b.hasOrphan(BLK02.header.hash) shouldBe false
  }

  "hasOrphan" should "return false for a block on the blockchain" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.hasOrphan(BLK01.header.hash) shouldBe false

    b.acceptBlock(BLK02.header.hash, BLK02)
    b.hasOrphan(BLK02.header.hash) shouldBe false
  }

  "hasOrphan" should "return true for an orphan block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)
    b.hasOrphan(BLK02.header.hash) shouldBe true
    b.hasOrphan(BLK03a.header.hash) shouldBe true
  }


  "hasNonOrphan" should "return false for a non-existent block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.hasNonOrphan(BLK01.header.hash) shouldBe false
    b.hasNonOrphan(BLK02.header.hash) shouldBe false
  }

  "hasNonOrphan" should "return true for a block on the blockchain" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.hasNonOrphan(BLK01.header.hash) shouldBe true

    b.acceptBlock(BLK02.header.hash, BLK02)
    b.hasNonOrphan(BLK02.header.hash) shouldBe true
  }

  "hasNonOrphan" should "return false for an orphan block" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)
    b.hasNonOrphan(BLK02.header.hash) shouldBe false
    b.hasNonOrphan(BLK03a.header.hash) shouldBe false
  }


  "putOrphan" should "be able to put parent orphans first" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)
    b.putOrphan(BLK03b)
    b.putOrphan(BLK04a)

    b.hasOrphan(BLK01.header.hash) shouldBe false
    b.hasOrphan(BLK02.header.hash) shouldBe true
    b.hasOrphan(BLK03a.header.hash) shouldBe true
    b.hasOrphan(BLK03b.header.hash) shouldBe true
    b.hasOrphan(BLK04a.header.hash) shouldBe true

    chain.blockOrphanage.getOrphansDependingOn(BLK01.header.hash).toSet shouldBe Set(BLK02.header.hash)
    chain.blockOrphanage.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
    chain.blockOrphanage.getOrphansDependingOn(BLK03a.header.hash).toSet shouldBe Set(BLK04a.header.hash)
    chain.blockOrphanage.getOrphansDependingOn(BLK03b.header.hash).toSet shouldBe Set()
  }

  "putOrphan" should "be able to put child orphans first" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK04a)
    b.putOrphan(BLK03b)
    b.putOrphan(BLK03a)
    b.putOrphan(BLK02)

    b.hasOrphan(BLK01.header.hash) shouldBe false
    b.hasOrphan(BLK02.header.hash) shouldBe true
    b.hasOrphan(BLK03a.header.hash) shouldBe true
    b.hasOrphan(BLK03b.header.hash) shouldBe true
    b.hasOrphan(BLK04a.header.hash) shouldBe true


    chain.blockOrphanage.getOrphansDependingOn(BLK01.header.hash).toSet shouldBe Set(BLK02.header.hash)
    chain.blockOrphanage.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
    chain.blockOrphanage.getOrphansDependingOn(BLK03a.header.hash).toSet shouldBe Set(BLK04a.header.hash)
    chain.blockOrphanage.getOrphansDependingOn(BLK03b.header.hash).toSet shouldBe Set()
  }


  "getOrphanRoot" should "return itself if the parent of it is missing" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)

    b.getOrphanRoot(BLK02.header.hash) shouldBe BLK02.header.hash

  }

  "getOrphanRoot" should "return an orphan which misses a parent" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)

    b.getOrphanRoot(BLK03a.header.hash) shouldBe BLK02.header.hash
  }

  "getOrphanRoot" should "return an orphan which misses a parent, and even though it has a child" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.putOrphan(BLK02)
    b.putOrphan(BLK03a)
    b.putOrphan(BLK04a)

    b.getOrphanRoot(BLK03a.header.hash) shouldBe BLK02.header.hash
  }


  // Need to implement test cases for validateBlock
  "validateBlock" should "" ignore {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

  }


  // TODO : Enable the test after implementing BlockMagnet.
  "acceptChildren" should "accept all children" ignore {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    /*
    With the following dependency between blocks, B02 will be missing, and acceptBlock(B02) will be invoked.

       Genesis → B01(4) → B02(4) → B03a(4) → B04a(4) → B05a(8)
                                 ↘ B03b(4) → B04b(8)
    */
    b.putOrphan( BLK03a )
    b.putOrphan( BLK04a )
    b.putOrphan( BLK05a )
    b.putOrphan( BLK03b )
    b.putOrphan( BLK04b )

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)


    val acceptedChildren : List<Hash> = b.acceptChildren(BLK02.header.hash)

    acceptedChildren.toSet shouldBe Set(BLK03a.header.hash, BLK04a.header.hash, BLK05a.header.hash, BLK03b.header.hash, BLK04b.header.hash )

    chain.blockOrphanage.getOrphansDependingOn(BLK02.header.hash) shouldBe List()
    chain.blockOrphanage.getOrphansDependingOn(BLK03a.header.hash) shouldBe List()
    chain.blockOrphanage.getOrphansDependingOn(BLK04a.header.hash) shouldBe List()
    chain.blockOrphanage.getOrphansDependingOn(BLK05a.header.hash) shouldBe List()
    chain.blockOrphanage.getOrphansDependingOn(BLK03b.header.hash) shouldBe List()
    chain.blockOrphanage.getOrphansDependingOn(BLK04b.header.hash) shouldBe List()

    chain.blockOrphanage.hasOrphan(BLK02.header.hash) shouldBe false

    chain.blockOrphanage.hasOrphan(BLK03a.header.hash) shouldBe false
    chain.blockOrphanage.hasOrphan(BLK03a.header.hash) shouldBe false
    chain.blockOrphanage.hasOrphan(BLK04a.header.hash) shouldBe false
    chain.blockOrphanage.hasOrphan(BLK05a.header.hash) shouldBe false
    chain.blockOrphanage.hasOrphan(BLK03b.header.hash) shouldBe false
    chain.blockOrphanage.hasOrphan(BLK04b.header.hash) shouldBe false

    b.getBlock(BLK02.header.hash) shouldBe Some(BLK02)
    b.getBlock(BLK03a.header.hash) shouldBe Some(BLK03a)
    b.getBlock(BLK04a.header.hash) shouldBe Some(BLK04a)
    b.getBlock(BLK05a.header.hash) shouldBe Some(BLK05a)
    b.getBlock(BLK03b.header.hash) shouldBe Some(BLK03b)
    b.getBlock(BLK04b.header.hash) shouldBe Some(BLK04b)
  }

  "acceptChildren" should "accept nothing if no child exists" in {
    val data = BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)

    val acceptedChildren : List<Hash> = b.acceptChildren(BLK01.header.hash)

    acceptedChildren shouldBe List()
  }

  /*
  // Need to implement after implementing headers-first approach.
    "acceptBlockHeader" should "" ignore {
    }

    "getBlockHeader" should "" ignore {
    }
  */
}