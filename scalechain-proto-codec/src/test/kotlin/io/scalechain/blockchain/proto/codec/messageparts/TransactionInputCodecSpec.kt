package io.scalechain.blockchain.proto.codec.messageparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.TransactionInput
import io.scalechain.blockchain.proto.codec.TransactionInputCodec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactionInputCodecSpec : FlatSpec(), Matchers {

  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  init {
    "generationOrNormalToNormalTx" should "hit an assertion if the input is neither GenerationTransactionInput nor NormalTransactionInput" {
      shouldThrow<AssertionError> {
        TransactionInputCodec.generationOrNormalToNormalTx(
          object : TransactionInput {
            override val outputTransactionHash : Hash = Hash.ALL_ZERO
            override val outputIndex : Long = 0
          }
        )
      }
    }
  }
}
