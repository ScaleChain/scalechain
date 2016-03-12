package io.scalechain.blockchain.storage.record

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockFileNameSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  override def beforeEach() {
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

  }

  "apply" should "" in {
  }

  "unapply" should "" in {
  }

}
