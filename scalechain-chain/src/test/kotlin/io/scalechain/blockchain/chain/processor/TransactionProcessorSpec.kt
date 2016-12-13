package io.scalechain.blockchain.chain.processor

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.proto.InvType
import io.scalechain.blockchain.proto.InvVector
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactionProcessorSpec : BlockchainTestTrait(), TransactionTestDataTrait, Matchers {

  override val testPath = File("./target/unittests-TransactionProcessorSpec/")
  lateinit var t : TransactionProcessor
  lateinit var b : BlockProcessor


  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    t = TransactionProcessor(chain)
    b = BlockProcessor(db, chain)

    // Put the genesis block for testing.
    b.acceptBlock(env().GenesisBlockHash, env().GenesisBlock)
  }

  override fun afterEach() {
    super.afterEach()
    // finalize a test.
  }

  init {

    "exists" should "return true for a non-orphan transaction in a block" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.acceptBlock(B.BLK03a.header.hash(), B.BLK03a)
      t.exists(db, T.TX03.transaction.hash()) shouldBe true
    }

    "exists" should "return true for a non-orphan transaction in the transaction pool" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      t.putTransaction(db, T.TX03.transaction.hash(), T.TX03.transaction)
      t.exists(db, T.TX03.transaction.hash()) shouldBe true
    }

    "exists" should "return true for an orphan transaction" {
      val data = BlockSampleData(db)
      val T = data.Tx

      t.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction)
      t.exists(db, T.TX03.transaction.hash()) shouldBe true
    }

    "exists" should "return false for a non-existent transaction" {
      val data = BlockSampleData(db)
      val T = data.Tx

      t.exists(db, T.TX02.transaction.hash()) shouldBe false
    }

    "getTransaction" should "return Some(non-orphan transaction) in a block" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.acceptBlock(B.BLK03a.header.hash(), B.BLK03a)
      t.getTransaction(db, T.TX03.transaction.hash())!! shouldBe T.TX03.transaction
    }

    "getTransaction" should "return Some(non-orphan transaction) in the transaction pool" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      t.putTransaction(db, T.TX03.transaction.hash(), T.TX03.transaction)
      t.getTransaction(db, T.TX03.transaction.hash())!! shouldBe T.TX03.transaction
    }

    "getTransaction" should "return None for an orphan transaction" {
      val data = BlockSampleData(db)
      val T = data.Tx

      t.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction)
      t.getTransaction(db, T.TX03.transaction.hash()) shouldBe null
    }

    "getTransaction" should "return None for a non-existent transaction" {
      val data = BlockSampleData(db)
      val T = data.Tx

      t.getTransaction(db, T.TX03.transaction.hash()) shouldBe null
    }

    "putTransaction" should "add a transaction in the pool" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      t.putTransaction(db, T.TX03.transaction.hash(), T.TX03.transaction)
      t.chain.txPool.getOldestTransactions(db, 100).contains(Pair(T.TX03.transaction.hash(), T.TX03.transaction)) shouldBe true
    }

    "acceptChildren" should "accept all children" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      t.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )
      t.putOrphan(db, T.TX04.transaction.hash(), T.TX04.transaction )

      /**
        * Transaction Dependency before calling acceptChildren
        *
        *  T.TX02 → T.TX03
        *    ↘       ↘
        *      ↘ → → → → T.TX04
        */

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)

      t.putTransaction(db, T.TX02.transaction.hash(), T.TX02.transaction)
      val acceptedChildren : List<Hash> = t.acceptChildren(db, T.TX02.transaction.hash())
      acceptedChildren.toSet() shouldBe setOf(T.TX03.transaction.hash(), T.TX04.transaction.hash())
      t.chain.txOrphanage.getOrphansDependingOn(db, T.TX02.transaction.hash()) shouldBe listOf<Hash>()
      t.chain.txOrphanage.getOrphansDependingOn(db, T.TX03.transaction.hash()) shouldBe listOf<Hash>()
      t.chain.txOrphanage.getOrphansDependingOn(db, T.TX04.transaction.hash()) shouldBe listOf<Hash>()

      t.chain.txOrphanage.getOrphan(db, T.TX02.transaction.hash()) shouldBe null
      t.chain.txOrphanage.getOrphan(db, T.TX03.transaction.hash()) shouldBe null
      t.chain.txOrphanage.getOrphan(db, T.TX04.transaction.hash()) shouldBe null

      t.getTransaction(db, T.TX02.transaction.hash()) shouldBe T.TX02.transaction
      t.getTransaction(db, T.TX03.transaction.hash()) shouldBe T.TX03.transaction
      t.getTransaction(db, T.TX04.transaction.hash()) shouldBe T.TX04.transaction
    }

    "acceptChildren" should "accept nothing if no child exists" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)

      t.putTransaction(db, T.TX02.transaction.hash(), T.TX02.transaction)

      val acceptedChildren : List<Hash> = t.acceptChildren(db, T.TX02.transaction.hash())

      acceptedChildren shouldBe listOf<Hash>()
    }

    "putOrphan" should "put an orphan" {
      val data = BlockSampleData(db)
      val T = data.Tx

      t.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction)
      t.chain.txOrphanage.getOrphan(db, T.TX03.transaction.hash())!! shouldBe T.TX03.transaction
    }
  }
}