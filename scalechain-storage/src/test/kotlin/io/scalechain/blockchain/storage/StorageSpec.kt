package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
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
      Storage.isInitialized shouldBe true
    }
  }
}
