package io.scalechain.blockchain.proto.codec.messageparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.TransactionInput
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactionInputSpec : FlatSpec(), Matchers {

  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  init {
    "" should "" {
      object : TransactionInput {
      }
    }
  }
}
