package io.scalechain.blockchain.storage

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class TransientTransactionStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "put(tx)" should "" in {
  }

  "put(txHash,tx)" should "" in {
  }

  "get(txHash)" should "" in {
  }

  "del(txHash)" should "" in {
  }

  "exists" should "" in {
  }
}

