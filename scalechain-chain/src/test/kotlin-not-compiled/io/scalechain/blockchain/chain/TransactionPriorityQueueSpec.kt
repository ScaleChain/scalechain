package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest.*
import io.scalechain.blockchain.script.HashSupported.*

/**
  * Created by kangmo on 6/30/16.
  */
class TransactionPriorityQueueSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-TransactionPriorityQueueSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  var q : TransactionPriorityQueue = null

  var data : TransactionSampleData = null

  override fun beforeEach() {
    super.beforeEach()

    keyValueDB = db

    data = TransactionSampleData()
    val d = data
    import d.*
    import d.Tx.*
    import d.Block.*

    // put the genesis block
    chain.putBlock(data.env.GenesisBlockHash, data.env.GenesisBlock)
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    chain.putBlock(BLK03.header.hash, BLK03)

    q = TransactionPriorityQueue(chain)
  }

  override fun afterEach() {
    super.afterEach()

    q = null
    db = null
    // finalize a test.
  }

  "enqueue" should "throw ChainException if the required transaction is not found" {
    val d = data
    import d.*
    import d.Tx.*
    import d.Block.*

    a<ChainException> should be thrownBy {
      q.enqueue(TX04_02.transaction)
    }
  }


  "enqueue" should "enqueue transaction if all required transactions exist" {
    val d = data
    import d.*
    import d.Tx.*
    import d.Block.*

    val inputTransactions = listOf(
      TX04_01, // fee 1 SC
      TX04_02, // fee 2 SC
      TX04_03, // fee 4 SC
      TX04_04, // fee 12 SC
      TX04_05_01, // fee 8 SC
      TX04_05_02, // fee 6 SC
      TX04_05_03, // fee 4 SC
      TX04_05_04, // fee 2 SC
      TX04_05_05  // fee 0 SC
    )

    inputTransactions foreach { tx =>
      //println(s"Adding transaction : ${tx.name}")
      chain.putTransaction(tx.transaction.hash, tx.transaction)
      q.enqueue(tx.transaction)
    }

    q.dequeue shouldBe TX04_04.transaction)    // fee 12 SC
    q.dequeue shouldBe TX04_05_01.transaction) // fee 8 SC
    q.dequeue shouldBe TX04_05_02.transaction) // fee 6 SC
    Set(TX04_03.transaction, TX04_05_03.transaction) should contain (q.dequeue.get) // fee 4 SC
    Set(TX04_03.transaction, TX04_05_03.transaction) should contain (q.dequeue.get) // fee 4 SC
    Set(TX04_02.transaction, TX04_05_04.transaction) should contain (q.dequeue.get) // fee 2 SC
    Set(TX04_02.transaction, TX04_05_04.transaction) should contain (q.dequeue.get) // fee 2 SC
    q.dequeue shouldBe TX04_01.transaction)    // fee 1 SC
    q.dequeue shouldBe TX04_05_05.transaction) // fee 0 SC
    q.dequeue shouldBe null
  }

  "dequeue" should "return None if enqueue was not invoked" {
    val d = data
    import d.*
    import d.Tx.*
    import d.Block.*

    q.dequeue shouldBe null
  }

}
