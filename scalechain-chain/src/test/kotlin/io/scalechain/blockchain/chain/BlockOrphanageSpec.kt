package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import java.io.File

import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import io.kotlintest.matchers.Matchers
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/16/16.
  */
@RunWith(KTestJUnitRunner::class)
class BlockOrphanageSpec : BlockchainTestTrait(), TransactionTestDataTrait, Matchers {

  override val testPath = File("./target/unittests-BlockOrphangeSpec/")

  lateinit var o: BlockOrphanage

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    // put the genesis block
    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)

    o = chain.blockOrphanage
  }

  override fun afterEach() {

    super.afterEach()

    // finalize a test.
  }

  init {


    "hasOrphan" should "return false for a non-existent orphan" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.hasOrphan(db, B.BLK02.header.hash()) shouldBe false
    }

    "hasOrphan" should "return true for a orphan block" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK02)
      o.hasOrphan(db, B.BLK02.header.hash()) shouldBe true
    }

    "putOrphan" should "be able to put child orphans first" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)
      o.putOrphan(db, B.BLK02)

      o.hasOrphan(db, B.BLK02.header.hash()) shouldBe true
      o.hasOrphan(db, B.BLK03a.header.hash()) shouldBe true
      o.hasOrphan(db, B.BLK03b.header.hash()) shouldBe true
      o.getOrphansDependingOn(db, B.BLK03a.header.hash()) shouldBe listOf<Hash>()
      o.getOrphansDependingOn(db, B.BLK03b.header.hash()) shouldBe listOf<Hash>()
      o.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())
    }

    "putOrphan" should "be able to put parent orphans first" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK02)
      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)

      o.hasOrphan(db, B.BLK02.header.hash()) shouldBe true
      o.hasOrphan(db, B.BLK03a.header.hash()) shouldBe true
      o.hasOrphan(db, B.BLK03b.header.hash()) shouldBe true
      o.getOrphansDependingOn(db, B.BLK03a.header.hash()) shouldBe listOf<Hash>()
      o.getOrphansDependingOn(db, B.BLK03b.header.hash()) shouldBe listOf<Hash>()
      o.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())
    }

    "putOrphan" should "be able to put orphans in mixed order" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)
      o.putOrphan(db, B.BLK02)

      o.hasOrphan(db, B.BLK02.header.hash()) shouldBe true
      o.hasOrphan(db, B.BLK03a.header.hash()) shouldBe true
      o.hasOrphan(db, B.BLK03b.header.hash()) shouldBe true
      o.getOrphansDependingOn(db, B.BLK03a.header.hash()) shouldBe listOf<Hash>()
      o.getOrphansDependingOn(db, B.BLK03b.header.hash()) shouldBe listOf<Hash>()
      o.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())
    }


    "getOrphan" should "return None for a non-existent orphan" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.getOrphan(db, B.BLK02.header.hash()) shouldBe null
    }


    "getOrphan" should "return an orphan" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)
      o.putOrphan(db, B.BLK02)

      o.getOrphan(db, B.BLK03a.header.hash()) shouldBe B.BLK03a
      o.getOrphan(db, B.BLK03b.header.hash()) shouldBe B.BLK03b
      o.getOrphan(db, B.BLK02.header.hash()) shouldBe B.BLK02

    }


    "delOrphan" should "delete an orphan" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK02)
      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)

      o.delOrphan(db, B.BLK02)
      o.hasOrphan(db, B.BLK02.header.hash()) shouldBe false

      o.delOrphan(db, B.BLK03b)
      o.hasOrphan(db, B.BLK03b.header.hash()) shouldBe false

      // The B.BLK03a was not deleted.
      o.hasOrphan(db, B.BLK03a.header.hash()) shouldBe true

    }

    "getOrphanRoot" should "return itself if the parent of it is missing" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK02)

      o.getOrphanRoot(db, B.BLK02.header.hash()) shouldBe B.BLK02.header.hash()
    }

    "getOrphanRoot" should "return an orphan which misses a parent" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK02)
      o.putOrphan(db, B.BLK03a)

      o.getOrphanRoot(db, B.BLK03a.header.hash()) shouldBe B.BLK02.header.hash()
    }


    "getOrphanRoot" should "return an orphan which misses a parent, and even though it has a child" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK02)
      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK04a)

      o.getOrphanRoot(db, B.BLK03a.header.hash()) shouldBe B.BLK02.header.hash()
    }

    "getOrphansDependingOn" should "return depending orphans if even though the parent was not put yet" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)
      o.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())
    }

    "removeDependenciesOn" should "remove dependent blocks for a block" {
      val data = BlockSampleData(db)
      val B = data.Block

      o.putOrphan(db, B.BLK03a)
      o.putOrphan(db, B.BLK03b)

      o.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())

      o.removeDependenciesOn(db, B.BLK02.header.hash())

      o.getOrphansDependingOn(db, B.BLK02.header.hash()) shouldBe listOf<Hash>()
    }
  }
}
