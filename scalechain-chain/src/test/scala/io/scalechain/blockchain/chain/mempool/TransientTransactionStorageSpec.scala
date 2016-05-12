package io.scalechain.blockchain.chain.mempool

import io.scalechain.blockchain.storage.test.TestData
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class TransientTransactionStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  import TestData._

  var storage : TransientTransactionStorage = null
  override def beforeEach() {
    storage = new TransientTransactionStorage()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage = null
  }

  "get(txHash)" should "get None if the transaction is not found " in {
    storage.get(txHash1) shouldBe None
    storage.get(txHash2) shouldBe None
  }
/*
  "get(txHash) after put(tx)" should "get some transaction if the transaction is found " in {
    storage.put(transaction1)
    storage.get(txHash1) shouldBe Some(transaction1)

    storage.put(transaction2)
    storage.get(txHash2) shouldBe Some(transaction2)
    storage.get(txHash1) shouldBe Some(transaction1)
  }
*/
  "get(txHash) after put(txHash,tx)" should "get some transaction if the transaction is found " in {
    // Use txHash2 for transaction1, txHash1 for transaction2.
    storage.put(txHash2, transaction1)
    storage.get(txHash2) shouldBe Some(transaction1)

    storage.put(txHash1, transaction2)
    storage.get(txHash1) shouldBe Some(transaction2)
    storage.get(txHash2) shouldBe Some(transaction1)
  }

  "put(txHash,tx)" should "be able to overwrite an existing transaction" in {
    storage.put(txHash1, transaction1)
    storage.get(txHash1) shouldBe Some(transaction1)

    storage.put(txHash1, transaction2)
    storage.get(txHash1) shouldBe Some(transaction2)
  }

  "put(tx)" should "be able to overwrite an existing transaction" in {
    storage.put(txHash1, transaction2)
    storage.get(txHash1) shouldBe Some(transaction2)

    storage.put(txHash1, transaction1)
    storage.get(txHash1) shouldBe Some(transaction1)
  }


  "del(txHash)" should "not throw an exception when the transaction hash does not exist." in {
    storage.del(txHash1)
  }

  "del(txHash)" should "delete an existing entry" in {
    storage.put(txHash1, transaction1)
    storage.get(txHash1) shouldBe Some(transaction1)
    storage.del(txHash1)

    storage.get(txHash1) shouldBe None
  }

  "exists" should "return true only if a transaction exists" in {
    storage.exists(txHash1) shouldBe false

    storage.put(txHash1, transaction1)
    storage.exists(txHash1) shouldBe true

    storage.del(txHash1)
    storage.exists(txHash1) shouldBe false
  }
}

