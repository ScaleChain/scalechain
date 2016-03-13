package io.scalechain.blockchain.storage

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockWriterSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "appendBlock" should "write a block and produce valid locators for each transaction" in {
    // Step 1 : Write a block

    // Step 2 : Read each transaction on each transaction locator.

    // Step 3 : Make sure the transaction hash matches.
  }
}

