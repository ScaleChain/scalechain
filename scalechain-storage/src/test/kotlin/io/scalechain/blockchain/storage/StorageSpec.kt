package io.scalechain.blockchain.storage

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

/**
  * Created by kangmo on 11/2/15.
  */
class StorageSpec : FlatSpec(), Matchers {

  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

  }

  init {
    "initialized" should "return true after initialize is invoked" {
      Storage.initialize()
      Storage.initialized() shouldBe true
    }
  }
}
