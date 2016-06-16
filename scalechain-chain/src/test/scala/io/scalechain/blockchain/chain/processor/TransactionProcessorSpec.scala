package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{BlockSampleData, BlockchainTestTrait}
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import HashSupported._
import org.scalatest._

class TransactionProcessorSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionProcessorTestTrait/")
  var t : TransactionProcessor = null
  var b : BlockProcessor = null
  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
    t = new TransactionProcessor(chain)
    b = new BlockProcessor(chain)
    // Put the genesis block for testing.
    b.acceptBlock(env.GenesisBlockHash, env.GenesisBlock)
  }

  override def afterEach() {
    super.afterEach()
    t = null
    b = null
    // finalize a test.
  }

  "exists" should "return true for a non-orphan transaction in a block" in {
    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    b.acceptBlock(BLK03a.header.hash, BLK03a)
    t.exists(TX03.transaction.hash) shouldBe true
  }

  "exists" should "return true for a non-orphan transaction in the transaction pool" in {
    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    t.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    t.exists(TX03.transaction.hash) shouldBe true
  }

  "exists" should "return true for an orphan transaction" in {
    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.exists(TX03.transaction.hash) shouldBe true
  }

  "exists" should "return false for a non-existent transaction" in {
    t.exists(TX02.transaction.hash) shouldBe false
  }

  "getTransaction" should "return Some(non-orphan transaction) in a block" in {
    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    b.acceptBlock(BLK03a.header.hash, BLK03a)
    t.getTransaction(TX03.transaction.hash).get shouldBe TX03.transaction
  }

  "getTransaction" should "return Some(non-orphan transaction) in the transaction pool" in {
    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    t.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    t.getTransaction(TX03.transaction.hash).get shouldBe TX03.transaction
  }

  "getTransaction" should "return None for an orphan transaction" in {
    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.getTransaction(TX03.transaction.hash) shouldBe None
  }

  "getTransaction" should "return None for a non-existent transaction" in {
    t.getTransaction(TX03.transaction.hash) shouldBe None
  }

  "addTransactionToPool" should "add a transaction in the pool" in {
    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    t.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    t.chain.txPool.getTransactionsFromPool should contain (TX03.transaction.hash, TX03.transaction)
  }

  "acceptChildren" should "" in {
  }

  "putOrphan" should "put an orphan" in {
    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.chain.txOrphange.getOrphan(TX03.transaction.hash).get shouldBe TX03.transaction
  }

  "delOrphans" should "del sorphans" in {
    t.putOrphan(TX03.transaction.hash, TX03.transaction)
    t.putOrphan(TX04.transaction.hash, TX04.transaction)
    t.putOrphan(TX05a.transaction.hash, TX05a.transaction)

    t.delOrphans(List(TX04.transaction.hash, TX05a.transaction.hash))

    t.chain.txOrphange.getOrphan(TX03.transaction.hash).get shouldBe TX03.transaction
    t.chain.txOrphange.getOrphan(TX04.transaction.hash) shouldBe None
    t.chain.txOrphange.getOrphan(TX05a.transaction.hash) shouldBe None
  }
}