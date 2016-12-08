package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest.*
import HashSupported.*

/**
  * Created by kangmo on 6/16/16.
  */
class BlockOrphanageSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-BlockOrphangeSpec/")

  implicit var keyValueDB : KeyValueDatabase = null


  var o: BlockOrphanage = null

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    keyValueDB = db

    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    o = chain.blockOrphanage
  }

  override fun afterEach() {
    keyValueDB = null
    o = null

    super.afterEach()

    // finalize a test.
  }

  "hasOrphan" should "return false for a non-existent orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.hasOrphan(BLK02.header.hash) shouldBe false
  }

  "hasOrphan" should "return true for a orphan block" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK02)
    o.hasOrphan(BLK02.header.hash) shouldBe true
  }

  "putOrphan" should "be able to put child orphans first" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.putOrphan(BLK02)

    o.hasOrphan(BLK02.header.hash) shouldBe true
    o.hasOrphan(BLK03a.header.hash) shouldBe true
    o.hasOrphan(BLK03b.header.hash) shouldBe true
    o.getOrphansDependingOn(BLK03a.header.hash) shouldBe listOf()
    o.getOrphansDependingOn(BLK03b.header.hash) shouldBe listOf()
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }

  "putOrphan" should "be able to put parent orphans first" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)

    o.hasOrphan(BLK02.header.hash) shouldBe true
    o.hasOrphan(BLK03a.header.hash) shouldBe true
    o.hasOrphan(BLK03b.header.hash) shouldBe true
    o.getOrphansDependingOn(BLK03a.header.hash) shouldBe listOf()
    o.getOrphansDependingOn(BLK03b.header.hash) shouldBe listOf()
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }

  "putOrphan" should "be able to put orphans in mixed order" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.putOrphan(BLK02)

    o.hasOrphan(BLK02.header.hash) shouldBe true
    o.hasOrphan(BLK03a.header.hash) shouldBe true
    o.hasOrphan(BLK03b.header.hash) shouldBe true
    o.getOrphansDependingOn(BLK03a.header.hash) shouldBe listOf()
    o.getOrphansDependingOn(BLK03b.header.hash) shouldBe listOf()
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }


  "getOrphan" should "return None for a non-existent orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.getOrphan(BLK02.header.hash) shouldBe null
  }


  "getOrphan" should "return an orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.putOrphan(BLK02)

    o.getOrphan(BLK03a.header.hash) shouldBe BLK03a)
    o.getOrphan(BLK03b.header.hash) shouldBe BLK03b)
    o.getOrphan(BLK02.header.hash) shouldBe BLK02)

  }


  "delOrphan" should "delete an orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

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

  "getOrphanRoot" should "return itself if the parent of it is missing" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK02)

    o.getOrphanRoot(BLK02.header.hash) shouldBe BLK02.header.hash
  }

  "getOrphanRoot" should "return an orphan which misses a parent" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)

    o.getOrphanRoot(BLK03a.header.hash) shouldBe BLK02.header.hash
  }


  "getOrphanRoot" should "return an orphan which misses a parent, and even though it has a child" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK02)
    o.putOrphan(BLK03a)
    o.putOrphan(BLK04a)

    o.getOrphanRoot(BLK03a.header.hash) shouldBe BLK02.header.hash
  }

  "getOrphansDependingOn" should "return depending orphans if even though the parent was not put yet" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)
    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)
  }

  "removeDependenciesOn" should "remove dependent blocks for a block" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

    o.putOrphan(BLK03a)
    o.putOrphan(BLK03b)

    o.getOrphansDependingOn(BLK02.header.hash).toSet shouldBe Set(BLK03a.header.hash, BLK03b.header.hash)

    o.removeDependenciesOn(BLK02.header.hash)

    o.getOrphansDependingOn(BLK02.header.hash).toList shouldBe listOf()
  }
}
