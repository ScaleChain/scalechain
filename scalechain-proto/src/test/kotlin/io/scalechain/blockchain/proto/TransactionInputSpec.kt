package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.ListExt
import org.junit.runner.RunWith

class MyTransactionInput(override val outputTransactionHash : Hash,
                         override val outputIndex : Long) : TransactionInput

@RunWith(KTestJUnitRunner::class)
class TransactionInputSpec : FlatSpec(), Matchers {
    override fun beforeEach() {
        // Need to create a subclass of TransactionInput to test this case.

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

    }

    init {
        "getOutPoint" should "return an OutPoint constructed from outputTransactionHash and outputIndex" {
            MyTransactionInput(Hash.ALL_ZERO, 0xFFFFFFFFL).getOutPoint() shouldBe OutPoint(Hash.ALL_ZERO, -1)
            MyTransactionInput(Hash.ALL_ZERO, 0L).getOutPoint() shouldBe OutPoint(Hash.ALL_ZERO, 0)
            MyTransactionInput(TestData.ALL_ONE_HASH, 0xFFFFFFFFL).getOutPoint() shouldBe OutPoint(TestData.ALL_ONE_HASH, -1)
        }

        "isCoinBaseInput" should "return true if bytes in the outputTransactionHash is all zero" {
            MyTransactionInput(Hash.ALL_ZERO, 0xFFFFFFFFL).isCoinBaseInput() shouldBe true
            // BUGBUG : Need to check bits of the output index as well.
            MyTransactionInput(Hash.ALL_ZERO, 0L).isCoinBaseInput() shouldBe false

            MyTransactionInput(TestData.ALL_ONE_HASH, 0xFFFFFFFFL).isCoinBaseInput() shouldBe false
        }
    }
}