package io.scalechain.blockchain.storage

import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class StorageSpec : FlatSpec with BeforeAndAfterEach with Matchers {
  this: Suite =>


  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

  }

  "initialized" should "return true after initialize is invoked" in {
    Storage.initialize()
    Storage.initialized() shouldBe true
  }
}
