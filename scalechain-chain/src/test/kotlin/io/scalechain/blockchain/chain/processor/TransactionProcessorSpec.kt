package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{BlockSampleData, BlockchainTestTrait}
import io.scalechain.blockchain.proto.{InvType, InvVector, Hash}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import HashSupported._
import org.scalatest._

class TransactionProcessorSpec extends BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionProcessorSpec/")
  var t : TransactionProcessor = null
  var b : BlockProcessor = null
  implicit var keyValueDB : KeyValueDatabase = null


  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
    keyValueDB = db

    t = new TransactionProcessor(chain)
    b = new BlockProcessor(chain)

    // Put the genesis block for testing.
    b.acceptBlock(env.GenesisBlockHash, env.GenesisBlock)
  }

  override def afterEach() {
    super.afterEach()
    keyValueDB = null
    t = null
    b = null
    // finalize a test.
  }

  "exists" should "return true for a non-orphan transaction in a block" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    b.acceptBlock(BLK03a.header.hash, BLK03a)
    t.exists(TX03.transaction.hash) shouldBe true
  }

  "exists" should "return true for a non-orphan transaction in the transaction pool" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    t.putTransaction(TX03.transaction.hash, TX03.transaction)
    t.exists(TX03.transaction.hash) shouldBe true
  }

  "exists" should "return true for an orphan transaction" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.exists(TX03.transaction.hash) shouldBe true
  }

  "exists" should "return false for a non-existent transaction" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    t.exists(TX02.transaction.hash) shouldBe false
  }

  "getTransaction" should "return Some(non-orphan transaction) in a block" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    b.acceptBlock(BLK03a.header.hash, BLK03a)
    t.getTransaction(TX03.transaction.hash).get shouldBe TX03.transaction
  }

  "getTransaction" should "return Some(non-orphan transaction) in the transaction pool" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    t.putTransaction(TX03.transaction.hash, TX03.transaction)
    t.getTransaction(TX03.transaction.hash).get shouldBe TX03.transaction
  }

  "getTransaction" should "return None for an orphan transaction" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.getTransaction(TX03.transaction.hash) shouldBe None
  }

  "getTransaction" should "return None for a non-existent transaction" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    t.getTransaction(TX03.transaction.hash) shouldBe None
  }

  "putTransaction" should "add a transaction in the pool" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    t.putTransaction(TX03.transaction.hash, TX03.transaction)
    t.chain.txPool.getOldestTransactions(100) should contain (TX03.transaction.hash, TX03.transaction)
  }

  "acceptChildren" should "accept all children" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    t.putOrphan(TX03.transaction.hash, TX03.transaction )
    t.putOrphan(TX04.transaction.hash, TX04.transaction )

    /**
      * Transaction Dependency before calling acceptChildren
      *
      *  TX02 → TX03
      *    ↘       ↘
      *      ↘ → → → → TX04
      */

    b.acceptBlock(BLK01.header.hash, BLK01)

    t.putTransaction(TX02.transaction.hash, TX02.transaction)
    val acceptedChildren : List[Hash] = t.acceptChildren(TX02.transaction.hash)
    acceptedChildren.toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
    t.chain.txOrphanage.getOrphansDependingOn(TX02.transaction.hash) shouldBe List()
    t.chain.txOrphanage.getOrphansDependingOn(TX03.transaction.hash) shouldBe List()
    t.chain.txOrphanage.getOrphansDependingOn(TX04.transaction.hash) shouldBe List()

    t.chain.txOrphanage.getOrphan(TX02.transaction.hash) shouldBe None
    t.chain.txOrphanage.getOrphan(TX03.transaction.hash) shouldBe None
    t.chain.txOrphanage.getOrphan(TX04.transaction.hash) shouldBe None

    t.getTransaction(TX02.transaction.hash) shouldBe Some(TX02.transaction)
    t.getTransaction(TX03.transaction.hash) shouldBe Some(TX03.transaction)
    t.getTransaction(TX04.transaction.hash) shouldBe Some(TX04.transaction)
  }

  "acceptChildren" should "accept nothing if no child exists" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    b.acceptBlock(BLK01.header.hash, BLK01)

    t.putTransaction(TX02.transaction.hash, TX02.transaction)

    val acceptedChildren : List[Hash] = t.acceptChildren(TX02.transaction.hash)

    acceptedChildren shouldBe List()
  }

  "putOrphan" should "put an orphan" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.chain.txOrphanage.getOrphan(TX03.transaction.hash).get shouldBe TX03.transaction
  }
}