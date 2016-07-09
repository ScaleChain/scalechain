package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import io.scalechain.blockchain.script.HashSupported._

/**
  * Created by kangmo on 6/30/16.
  */
class TransactionPriorityQueueSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionPriorityQueueSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  var q : TransactionPriorityQueue = null

  var data : TransactionSampleData = null

  override def beforeEach() {
    super.beforeEach()

    keyValueDB = db

    data = new TransactionSampleData()
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    // put the genesis block
    chain.putBlock(data.env.GenesisBlockHash, data.env.GenesisBlock)
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    chain.putBlock(BLK03.header.hash, BLK03)

    q = new TransactionPriorityQueue(chain)
  }

  override def afterEach() {
    super.afterEach()

    q = null
    db = null
    // finalize a test.
  }

  "enqueue" should "throw ChainException if the required transaction is not found" in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    a[ChainException] should be thrownBy {
      q.enqueue(TX04_02.transaction)
    }
  }


  "enqueue" should "enqueue transaction if all required transactions exist" in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    val inputTransactions = List(
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

    q.dequeue shouldBe Some(TX04_04.transaction)    // fee 12 SC
    q.dequeue shouldBe Some(TX04_05_01.transaction) // fee 8 SC
    q.dequeue shouldBe Some(TX04_05_02.transaction) // fee 6 SC
    Set(TX04_03.transaction, TX04_05_03.transaction) should contain (q.dequeue.get) // fee 4 SC
    Set(TX04_03.transaction, TX04_05_03.transaction) should contain (q.dequeue.get) // fee 4 SC
    Set(TX04_02.transaction, TX04_05_04.transaction) should contain (q.dequeue.get) // fee 2 SC
    Set(TX04_02.transaction, TX04_05_04.transaction) should contain (q.dequeue.get) // fee 2 SC
    q.dequeue shouldBe Some(TX04_01.transaction)    // fee 1 SC
    q.dequeue shouldBe Some(TX04_05_05.transaction) // fee 0 SC
    q.dequeue shouldBe None
  }

  "dequeue" should "return None if enqueue was not invoked" in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    q.dequeue shouldBe None
  }

}
