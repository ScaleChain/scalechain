package io.scalechain.blockchain.chain

import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.TransactionTestDataTrait

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionPoolSpec : BlockchainTestTrait(), TransactionTestDataTrait, Matchers {
  override val testPath = File("./target/unittests-TransactionPoolSpec/")

  lateinit var p : TransactionPool

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    // put the genesis block
    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)

    p = chain.txPool
  }

  override fun afterEach() {

    super.afterEach()
  }

  init {
    "addTransactionToPool" should "add non-orphan transactions spending UT.TXOs" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      p.addTransactionToPool(db, T.TX03a.transaction.hash(), T.TX03a.transaction)
      p.addTransactionToPool(db, T.TX04.transaction.hash(), T.TX04.transaction)
      p.addTransactionToPool(db, T.TX04a.transaction.hash(), T.TX04a.transaction)
      p.addTransactionToPool(db, T.TX05a.transaction.hash(), T.TX05a.transaction)

      p.getOldestTransactions(db, 100).toSet() shouldBe setOf(
        Pair(T.TX03.transaction.hash(), T.TX03.transaction),
        Pair(T.TX03a.transaction.hash(), T.TX03a.transaction),
        Pair(T.TX04.transaction.hash(), T.TX04.transaction),
        Pair(T.TX04a.transaction.hash(), T.TX04a.transaction),
        Pair(T.TX05a.transaction.hash(), T.TX05a.transaction)
      )
    }

    "getOldestTransactions" should "not return removed transactions" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)

      p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      p.addTransactionToPool(db, T.TX03a.transaction.hash(), T.TX03a.transaction)
      p.addTransactionToPool(db, T.TX04.transaction.hash(), T.TX04.transaction)
      p.addTransactionToPool(db, T.TX04a.transaction.hash(), T.TX04a.transaction)
      p.addTransactionToPool(db, T.TX05a.transaction.hash(), T.TX05a.transaction)

      val candidateTransactions: List<Pair<Hash, Transaction>> = p.getOldestTransactions(db, 100)

      candidateTransactions.filter { pair ->
        // Because we are concurrently putting transactions into the pool while putting blocks,
        // There can be some transactions in the pool as well as on txDescIndex, where only transactions in a block is stored.
        // Skip all transactions that has the transaction descriptor.
        // If the transaction descriptor exists, it means the transaction is in a block.
        true
      }.forEach { pair ->
        val txHash = pair.first
        //val transaction = pair.second
        p.removeTransactionFromPool(db, txHash)
      }

      p.getOldestTransactions(db, 100).isEmpty() shouldBe true

    }

    "getOldestTransactions" should "should return the given maximum number of transactions" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      p.addTransactionToPool(db, T.TX03a.transaction.hash(), T.TX03a.transaction)
      p.addTransactionToPool(db, T.TX04.transaction.hash(), T.TX04.transaction)
      p.addTransactionToPool(db, T.TX04a.transaction.hash(), T.TX04a.transaction)
      p.addTransactionToPool(db, T.TX05a.transaction.hash(), T.TX05a.transaction)

      p.getOldestTransactions(db, 3).toSet() shouldBe setOf(
        Pair(T.TX03.transaction.hash(), T.TX03.transaction),
        Pair(T.TX03a.transaction.hash(), T.TX03a.transaction),
        Pair(T.TX04.transaction.hash(), T.TX04.transaction)
      )
    }

    "addTransactionToPool" should "add transactions even though there are some orphans depending on them" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)

      chain.txOrphanage.putOrphan(db, T.TX04.transaction.hash(), T.TX04.transaction)
      chain.txOrphanage.putOrphan(db, T.TX04a.transaction.hash(), T.TX04a.transaction)
      chain.txOrphanage.putOrphan(db, T.TX05a.transaction.hash(), T.TX05a.transaction)

      val txProcessor = TransactionProcessor(chain)

      p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      txProcessor.acceptChildren(db, T.TX03.transaction.hash())

      p.addTransactionToPool(db, T.TX03a.transaction.hash(), T.TX03a.transaction)
      txProcessor.acceptChildren(db, T.TX03a.transaction.hash())

      p.getOldestTransactions(db, 100).toSet() shouldBe setOf(
        Pair(T.TX03.transaction.hash(), T.TX03.transaction),
        Pair(T.TX03a.transaction.hash(), T.TX03a.transaction),
        Pair(T.TX04.transaction.hash(), T.TX04.transaction),
        Pair(T.TX04a.transaction.hash(), T.TX04a.transaction),
        Pair(T.TX05a.transaction.hash(), T.TX05a.transaction)
      )

      chain.txOrphanage.hasOrphan(db, T.TX03.transaction.hash()) shouldBe false
      chain.txOrphanage.hasOrphan(db, T.TX03a.transaction.hash()) shouldBe false
      chain.txOrphanage.hasOrphan(db, T.TX04.transaction.hash()) shouldBe false
      chain.txOrphanage.hasOrphan(db, T.TX04a.transaction.hash()) shouldBe false
      chain.txOrphanage.hasOrphan(db, T.TX05a.transaction.hash()) shouldBe false

      chain.txOrphanage.getOrphansDependingOn(db, T.TX03.transaction.hash()) shouldBe listOf<Hash>()
      chain.txOrphanage.getOrphansDependingOn(db, T.TX03a.transaction.hash()) shouldBe listOf<Hash>()
      chain.txOrphanage.getOrphansDependingOn(db, T.TX04.transaction.hash()) shouldBe listOf<Hash>()
      chain.txOrphanage.getOrphansDependingOn(db, T.TX04a.transaction.hash()) shouldBe listOf<Hash>()
      chain.txOrphanage.getOrphansDependingOn(db, T.TX05a.transaction.hash()) shouldBe listOf<Hash>()
    }

    "addTransactionToPool" should "throw an exception for an orphan transaction" {
      val data = BlockSampleData(db)
      val T = data.Tx

      val thrown = shouldThrow <ChainException> {
        p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      }
      thrown.code shouldBe ErrorCode.ParentTransactionNotFound
    }

    "addTransactionToPool" should "throw an exception for a double spending non-orphan transaction" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      p.addTransactionToPool(db, T.TX03a.transaction.hash(), T.TX03a.transaction)

      val thrown = shouldThrow<ChainException> {
        p.addTransactionToPool(db, T.TX03b.transaction.hash(), T.TX03b.transaction)
      }
      thrown.code shouldBe ErrorCode.TransactionOutputAlreadySpent
    }

    "removeTransactionFromPool" should "remove transactions from a pool" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      p.addTransactionToPool(db, T.TX03.transaction.hash(), T.TX03.transaction)
      p.addTransactionToPool(db, T.TX03a.transaction.hash(), T.TX03a.transaction)
      p.addTransactionToPool(db, T.TX04.transaction.hash(), T.TX04.transaction)
      p.addTransactionToPool(db, T.TX04a.transaction.hash(), T.TX04a.transaction)
      p.addTransactionToPool(db, T.TX05a.transaction.hash(), T.TX05a.transaction)

      // TODO : Need to hit an assertion when removing a transaction on which other transactions depend?
      p.removeTransactionFromPool(db, T.TX04.transaction.hash())
      p.removeTransactionFromPool(db, T.TX04a.transaction.hash())
      p.removeTransactionFromPool(db, T.TX05a.transaction.hash())

      p.getOldestTransactions(db, 100).toSet() shouldBe setOf(
        Pair(T.TX03.transaction.hash(), T.TX03.transaction),
        Pair(T.TX03a.transaction.hash(), T.TX03a.transaction)
      )
    }

    // getTransactionsFromPool Already tested by
    // 1. "addTransactionToPool" should "add transactions even though there are some orphans depending on them"
    // 2. "removeTransactionFromPool" should "remove transactions from a pool"
    /*
    "getTransactionsFromPool" should "" {
  
    }
    */
  }
}
