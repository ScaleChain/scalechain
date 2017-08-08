package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith


/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class BlockIndexSpec : FlatSpec(), Matchers {
  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  // BlockIndex is a trait. No need to create test cases for a trait.
  init {
    Storage.initialize()
  }
}

