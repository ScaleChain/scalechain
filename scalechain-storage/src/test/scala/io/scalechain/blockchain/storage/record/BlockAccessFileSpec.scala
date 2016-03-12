package io.scalechain.blockchain.storage.record

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockAccessFileSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "offset" should "" in {
  }

  "moveTo" should "" in {
  }

  "read" should "" in {
  }

  "write" should "" in {
  }

  "append" should "" in {
  }

  "flush" should "" in {
  }

}
