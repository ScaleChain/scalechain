package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class BigIntUtilSpec : FlatSpec(), Matchers {
    init {
        "bint" should "return a string representing the big integer" {
            val value = java.math.BigInteger("1")
            BigIntUtil.bint(value) shouldBe "BigInt(\"$value\")"
        }
    }
}

