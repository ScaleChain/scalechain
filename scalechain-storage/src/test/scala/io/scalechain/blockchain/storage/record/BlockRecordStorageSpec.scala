package io.scalechain.blockchain.storage.record

/**
  * Created by kangmo on 3/12/16.
  */

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockRecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "readRecord" should "successfully read a block after a block was appended" in {
  }

  "readRecord" should "successfully read a transaction after a block was appended" in {
  }
}
