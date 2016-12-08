package io.scalechain.blockchain.storage

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec


/**
  * Created by kangmo on 11/2/15.
  */
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

