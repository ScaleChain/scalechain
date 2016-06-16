package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.{ChainException, ErrorCode, RpcException}
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionPoolSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionPoolSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  var p : TransactionPool = null

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()

    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    p = chain.txPool
  }

  override def afterEach() {
    p = null

    super.afterEach()
  }

  "addTransactionToPool" should "add non-orphan transactions spending UTXOs" in {
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    p.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    p.addTransactionToPool(TX03a.transaction.hash, TX03a.transaction)
    p.addTransactionToPool(TX04.transaction.hash, TX04.transaction)
    p.addTransactionToPool(TX04a.transaction.hash, TX04a.transaction)
    p.addTransactionToPool(TX05a.transaction.hash, TX05a.transaction)

    p.getTransactionsFromPool().toSet shouldBe Set(
      (TX03.transaction.hash, TX03.transaction),
      (TX03a.transaction.hash, TX03a.transaction),
      (TX04.transaction.hash, TX04.transaction),
      (TX04a.transaction.hash, TX04a.transaction),
      (TX05a.transaction.hash, TX05a.transaction)
    )
  }

  "addTransactionToPool" should "add transactions even though there are some orphans depending on them" in {
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)

    chain.txOrphanage.putOrphan(TX04.transaction.hash, TX04.transaction)
    chain.txOrphanage.putOrphan(TX04a.transaction.hash, TX04a.transaction)
    chain.txOrphanage.putOrphan(TX05a.transaction.hash, TX05a.transaction)

    val txProcessor = new TransactionProcessor(chain)

    p.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    txProcessor.acceptChildren(TX03.transaction.hash)

    p.addTransactionToPool(TX03a.transaction.hash, TX03a.transaction)
    txProcessor.acceptChildren(TX03a.transaction.hash)

    p.getTransactionsFromPool().toSet shouldBe Set(
      (TX03.transaction.hash, TX03.transaction),
      (TX03a.transaction.hash, TX03a.transaction),
      (TX04.transaction.hash, TX04.transaction),
      (TX04a.transaction.hash, TX04a.transaction),
      (TX05a.transaction.hash, TX05a.transaction)
    )

    chain.txOrphanage.hasOrphan(TX03.transaction.hash) shouldBe false
    chain.txOrphanage.hasOrphan(TX03a.transaction.hash) shouldBe false
    chain.txOrphanage.hasOrphan(TX04.transaction.hash) shouldBe false
    chain.txOrphanage.hasOrphan(TX04a.transaction.hash) shouldBe false
    chain.txOrphanage.hasOrphan(TX05a.transaction.hash) shouldBe false

    chain.txOrphanage.getOrphansDependingOn(TX03.transaction.hash) shouldBe List()
    chain.txOrphanage.getOrphansDependingOn(TX03a.transaction.hash) shouldBe List()
    chain.txOrphanage.getOrphansDependingOn(TX04.transaction.hash) shouldBe List()
    chain.txOrphanage.getOrphansDependingOn(TX04a.transaction.hash) shouldBe List()
    chain.txOrphanage.getOrphansDependingOn(TX05a.transaction.hash) shouldBe List()
  }

  "addTransactionToPool" should "throw an exception for an orphan transaction" in {
    val thrown = the [ChainException] thrownBy {
      p.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    }
    thrown.code shouldBe ErrorCode.ParentTransactionNotFound
  }

  "addTransactionToPool" should "throw an exception for a double spending non-orphan transaction" in {
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    p.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    p.addTransactionToPool(TX03a.transaction.hash, TX03a.transaction)

    val thrown = the [ChainException] thrownBy {
      p.addTransactionToPool(TX03b.transaction.hash, TX03b.transaction)
    }
    thrown.code shouldBe ErrorCode.TransactionOutputAlreadySpent
  }

  "removeTransactionFromPool" should "remove transactions from a pool" in {
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    p.addTransactionToPool(TX03.transaction.hash, TX03.transaction)
    p.addTransactionToPool(TX03a.transaction.hash, TX03a.transaction)
    p.addTransactionToPool(TX04.transaction.hash, TX04.transaction)
    p.addTransactionToPool(TX04a.transaction.hash, TX04a.transaction)
    p.addTransactionToPool(TX05a.transaction.hash, TX05a.transaction)

    // TODO : Need to hit an assertion when removing a transaction on which other transactions depend?
    p.removeTransactionFromPool(TX04.transaction.hash)
    p.removeTransactionFromPool(TX04a.transaction.hash)
    p.removeTransactionFromPool(TX05a.transaction.hash)

    p.getTransactionsFromPool().toSet shouldBe Set(
      (TX03.transaction.hash, TX03.transaction),
      (TX03a.transaction.hash, TX03a.transaction)
    )
  }

  // getTransactionsFromPool Already tested by
  // 1. "addTransactionToPool" should "add transactions even though there are some orphans depending on them"
  // 2. "removeTransactionFromPool" should "remove transactions from a pool"
  /*
  "getTransactionsFromPool" should "" in {

  }
  */

}
