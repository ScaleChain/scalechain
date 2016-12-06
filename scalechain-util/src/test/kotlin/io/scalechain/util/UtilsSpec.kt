package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class UtilsSpec : FlatSpec(), Matchers {

    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
    }

    init {
        "bigIntegerToBytes" should "roundtrip" {
            for (i in 1..1000) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
                val random = SecureRandom ()
                random.setSeed(random.generateSeed(32))

                val originalKeyArray: kotlin.ByteArray = kotlin.ByteArray(32)
                assert(originalKeyArray.size == 32)
                random.nextBytes(originalKeyArray)

                val originalBigInteger = Utils.bytesToBigInteger(originalKeyArray)

                val encodedKeyArray = Utils.bigIntegerToBytes(originalBigInteger, 32)

                val createdBigInteger = Utils.bytesToBigInteger(encodedKeyArray)

                encodedKeyArray.toList() shouldBe originalKeyArray.toList()
                createdBigInteger shouldBe originalBigInteger
            }
        }
    }
}