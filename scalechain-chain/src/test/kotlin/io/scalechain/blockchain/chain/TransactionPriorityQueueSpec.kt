package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/30/16.
  */
@RunWith(KTestJUnitRunner::class)
class TransactionPriorityQueueSpec : BlockchainTestTrait(), TransactionTestInterface, Matchers {

  override val testPath = File("./build/unittests-TransactionPriorityQueueSpec/")

  lateinit var q : TransactionPriorityQueue

  lateinit var data : TransactionSampleData

  override fun beforeEach() {
    super.beforeEach()

    data = TransactionSampleData(db)
    val d = data
    val B = d.Block

    // put the genesis block
    chain.putBlock(db, data.env().GenesisBlockHash, data.env().GenesisBlock)
    chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
    chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
    chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

    q = TransactionPriorityQueue(chain)
  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
  }

  init {


    "enqueue" should "throw ChainException if the required transaction is not found" {
      val d = data
      val T = d.Tx

      shouldThrow<ChainException> {
        q.enqueue(db, T.TX04_02.transaction)
      }
    }


    "enqueue" should "enqueue transaction if all required transactions exist" {
      val d = data
      val T = d.Tx

      val inputTransactions = listOf(
        T.TX04_01, // fee 1 SC
        T.TX04_02, // fee 2 SC
        T.TX04_03, // fee 4 SC
        T.TX04_04, // fee 12 SC
        T.TX04_05_01, // fee 8 SC
        T.TX04_05_02, // fee 6 SC
        T.TX04_05_03, // fee 4 SC
        T.TX04_05_04, // fee 2 SC
        T.TX04_05_05  // fee 0 SC
      )

      inputTransactions.forEach { tx ->
        //println(s"Adding transaction : ${tx.name}")
        chain.putTransaction(db, tx.transaction.hash(), tx.transaction)
        q.enqueue(db, tx.transaction)
      }

      q.dequeue() shouldBe T.TX04_04.transaction    // fee 12 SC
      q.dequeue() shouldBe T.TX04_05_01.transaction // fee 8 SC
      q.dequeue() shouldBe T.TX04_05_02.transaction // fee 6 SC
      setOf(T.TX04_03.transaction, T.TX04_05_03.transaction) should contain (q.dequeue()!!) // fee 4 SC
      setOf(T.TX04_03.transaction, T.TX04_05_03.transaction) should contain (q.dequeue()!!) // fee 4 SC
      setOf(T.TX04_02.transaction, T.TX04_05_04.transaction) should contain (q.dequeue()!!) // fee 2 SC
      setOf(T.TX04_02.transaction, T.TX04_05_04.transaction) should contain (q.dequeue()!!) // fee 2 SC
      q.dequeue() shouldBe T.TX04_01.transaction    // fee 1 SC
      q.dequeue() shouldBe T.TX04_05_05.transaction // fee 0 SC
      q.dequeue() shouldBe null
    }

    "dequeue" should "return None if enqueue was not invoked" {
      q.dequeue() shouldBe null
    }
  }
}
