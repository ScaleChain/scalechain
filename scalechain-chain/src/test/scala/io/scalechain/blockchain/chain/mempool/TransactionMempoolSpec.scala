package io.scalechain.blockchain.chain.mempool

import java.io.File

import io.scalechain.blockchain.storage.{Storage, DiskBlockStorage}
import io.scalechain.blockchain.storage.test.TestData
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class TransactionMempoolSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()
  import TestData._

  var storage : TransactionMempool = null

  override def beforeEach() {
    storage = new TransactionMempool(DiskBlockStorage.create(new File(".")))

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage = null
  }

  "method" should "" in {
  }
}

