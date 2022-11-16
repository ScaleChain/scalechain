package io.scalechain.blockchain.chain.processor

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class BlockProcessorSpec : BlockchainTestTrait(), Matchers, TransactionTestInterface {

  override val testPath = File("./build/unittests-BlockProcessorSpec/")

  lateinit var b : BlockProcessor

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    b = BlockProcessor(db, chain)

    // Put the genesis block for testing.
    // Never pass the genesis block to acceptBlock. it should be passed to chain.putBlock.
    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)
  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
  }

  init {

    "getBlock" should "return Some(block) for a non-orphan block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.getBlock(B.BLK01.header.hash()) shouldBe B.BLK01

      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.getBlock(B.BLK02.header.hash()) shouldBe B.BLK02
    }

    "getBlock" should "return None for a non-existent block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.getBlock(B.BLK01.header.hash()) shouldBe null
      b.getBlock(B.BLK02.header.hash()) shouldBe null
    }

    "getBlock" should "return None for an orphan block block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)
      b.getBlock(B.BLK02.header.hash()) shouldBe null
      b.getBlock(B.BLK03a.header.hash()) shouldBe null
    }

    "exists" should "return false for a non-existent block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.exists(B.BLK01.header.hash()) shouldBe false
      b.exists(B.BLK02.header.hash()) shouldBe false
    }

    "exists" should "return true for a block on the blockchain" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.exists(B.BLK01.header.hash()) shouldBe true

      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.exists(B.BLK02.header.hash()) shouldBe true
    }

    "exists" should "return true for an orphan block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)
      b.exists(B.BLK02.header.hash()) shouldBe true
      b.exists(B.BLK03a.header.hash()) shouldBe true
    }


    "hasOrphan" should "return false for a non-existent block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.hasOrphan(B.BLK01.header.hash()) shouldBe false
      b.hasOrphan(B.BLK02.header.hash()) shouldBe false
    }

    "hasOrphan" should "return false for a block on the blockchain" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.hasOrphan(B.BLK01.header.hash()) shouldBe false

      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.hasOrphan(B.BLK02.header.hash()) shouldBe false
    }

    "hasOrphan" should "return true for an orphan block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)
      b.hasOrphan(B.BLK02.header.hash()) shouldBe true
      b.hasOrphan(B.BLK03a.header.hash()) shouldBe true
    }


    "hasNonOrphan" should "return false for a non-existent block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.hasNonOrphan(B.BLK01.header.hash()) shouldBe false
      b.hasNonOrphan(B.BLK02.header.hash()) shouldBe false
    }

    "hasNonOrphan" should "return true for a block on the blockchain" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.hasNonOrphan(B.BLK01.header.hash()) shouldBe true

      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.hasNonOrphan(B.BLK02.header.hash()) shouldBe true
    }

    "hasNonOrphan" should "return false for an orphan block" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)
      b.hasNonOrphan(B.BLK02.header.hash()) shouldBe false
      b.hasNonOrphan(B.BLK03a.header.hash()) shouldBe false
    }


    "putOrphan" should "be able to put parent orphans first" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)
      b.putOrphan(B.BLK03b)
      b.putOrphan(B.BLK04a)

      b.hasOrphan(B.BLK01.header.hash()) shouldBe false
      b.hasOrphan(B.BLK02.header.hash()) shouldBe true
      b.hasOrphan(B.BLK03a.header.hash()) shouldBe true
      b.hasOrphan(B.BLK03b.header.hash()) shouldBe true
      b.hasOrphan(B.BLK04a.header.hash()) shouldBe true

      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK01.header.hash()).toSet() shouldBe setOf(B.BLK02.header.hash())
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK03a.header.hash()).toSet() shouldBe setOf(B.BLK04a.header.hash())
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK03b.header.hash()).toSet() shouldBe setOf<Hash>()
    }

    "putOrphan" should "be able to put child orphans first" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK04a)
      b.putOrphan(B.BLK03b)
      b.putOrphan(B.BLK03a)
      b.putOrphan(B.BLK02)

      b.hasOrphan(B.BLK01.header.hash()) shouldBe false
      b.hasOrphan(B.BLK02.header.hash()) shouldBe true
      b.hasOrphan(B.BLK03a.header.hash()) shouldBe true
      b.hasOrphan(B.BLK03b.header.hash()) shouldBe true
      b.hasOrphan(B.BLK04a.header.hash()) shouldBe true


      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK01.header.hash()).toSet() shouldBe setOf(B.BLK02.header.hash())
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK02.header.hash()).toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK03b.header.hash())
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK03a.header.hash()).toSet() shouldBe setOf(B.BLK04a.header.hash())
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK03b.header.hash()).toSet() shouldBe setOf<Hash>()
    }


    "getOrphanRoot" should "return itself if the parent of it is missing" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)

      b.getOrphanRoot(B.BLK02.header.hash()) shouldBe B.BLK02.header.hash()

    }

    "getOrphanRoot" should "return an orphan which misses a parent" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)

      b.getOrphanRoot(B.BLK03a.header.hash()) shouldBe B.BLK02.header.hash()
    }

    "getOrphanRoot" should "return an orphan which misses a parent, and even though it has a child" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.putOrphan(B.BLK02)
      b.putOrphan(B.BLK03a)
      b.putOrphan(B.BLK04a)

      b.getOrphanRoot(B.BLK03a.header.hash()) shouldBe B.BLK02.header.hash()
    }


    // Need to implement test cases for validateBlock
    "validateBlock".config(ignored = true) should "" {
/*
      val data = BlockSampleData(db)
      val B = data.Block
*/
    }

    // TODO : Enable the test after implementing BlockMagnet.
    "acceptChildren".config(ignored = true) should "accept all children" {
      val data = BlockSampleData(db)
      val B = data.Block

      /*
      With the following dependency between blocks, B02 will be missing, and acceptBlock(B02) will be invoked.

         Genesis → B01(4) → B02(4) → B03a(4) → B04a(4) → B05a(8)
                                   ↘ B03b(4) → B04b(8)
      */
      b.putOrphan( B.BLK03a )
      b.putOrphan( B.BLK04a )
      b.putOrphan( B.BLK05a )
      b.putOrphan( B.BLK03b )
      b.putOrphan( B.BLK04b )

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)


      val acceptedChildren : List<Hash> = b.acceptChildren(B.BLK02.header.hash())

      acceptedChildren.toSet() shouldBe setOf(B.BLK03a.header.hash(), B.BLK04a.header.hash(), B.BLK05a.header.hash(), B.BLK03b.header.hash(), B.BLK04b.header.hash() )

      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK02.header.hash()) shouldBe listOf<Hash>()
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK03a.header.hash()) shouldBe listOf<Hash>()
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK04a.header.hash()) shouldBe listOf<Hash>()
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK05a.header.hash()) shouldBe listOf<Hash>()
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK03b.header.hash()) shouldBe listOf<Hash>()
      chain.blockOrphanage.getOrphansDependingOn(db, B.BLK04b.header.hash()) shouldBe listOf<Hash>()

      chain.blockOrphanage.hasOrphan(db, B.BLK02.header.hash()) shouldBe false

      chain.blockOrphanage.hasOrphan(db, B.BLK03a.header.hash()) shouldBe false
      chain.blockOrphanage.hasOrphan(db, B.BLK03a.header.hash()) shouldBe false
      chain.blockOrphanage.hasOrphan(db, B.BLK04a.header.hash()) shouldBe false
      chain.blockOrphanage.hasOrphan(db, B.BLK05a.header.hash()) shouldBe false
      chain.blockOrphanage.hasOrphan(db, B.BLK03b.header.hash()) shouldBe false
      chain.blockOrphanage.hasOrphan(db, B.BLK04b.header.hash()) shouldBe false

      b.getBlock(B.BLK02.header.hash()) shouldBe B.BLK02
      b.getBlock(B.BLK03a.header.hash()) shouldBe B.BLK03a
      b.getBlock(B.BLK04a.header.hash()) shouldBe B.BLK04a
      b.getBlock(B.BLK05a.header.hash()) shouldBe B.BLK05a
      b.getBlock(B.BLK03b.header.hash()) shouldBe B.BLK03b
      b.getBlock(B.BLK04b.header.hash()) shouldBe B.BLK04b
    }

    "acceptChildren" should "accept nothing if no child exists" {
      val data = BlockSampleData(db)
      val B = data.Block

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)

      val acceptedChildren : List<Hash> = b.acceptChildren(B.BLK01.header.hash())

      acceptedChildren shouldBe listOf<Hash>()
    }

    /*
    // Need to implement after implementing headers-first approach.
      "acceptBlockHeader" should "" ignore {
      }

      "getBlockHeader" should "" ignore {
      }
    */
  }
}