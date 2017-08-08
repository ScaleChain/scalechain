package io.scalechain.util

import io.kotlintest.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.*
import org.junit.runner.RunWith
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*
import kotlin.ByteArray

@RunWith(KTestJUnitRunner::class)
class Base58UtilSpec : FlatSpec(), Matchers {

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
        "Base58" should "encode byte arrays" {
            assert(Base58Util.encode("Hello World".toByteArray()) == "JxF12TrwUP45BMd")
            assert(Base58Util.encode(BigInteger.valueOf(3471844090L).toByteArray()) == "16Ho7Hs")

            println("aaa" + Base58Util.encode(ByteArray(1,{1})))
            assert(Base58Util.encode(ByteArray(1,{0})) == "1")
            assert(Base58Util.encode(ByteArray(7,{0})) == "1111111")
            assert(Base58Util.encode(ByteArray(0,{0})) == "")
        }

        "Base58" should "decode strings" {
            assert(java.util.Arrays.equals(Base58Util.decode("JxF12TrwUP45BMd"), "Hello World".toByteArray()))
            assert(java.util.Arrays.equals(Base58Util.decode(""), ByteArray(0, {0})))
            assert(java.util.Arrays.equals(Base58Util.decode("1"), ByteArray(1,{0})))
            assert(java.util.Arrays.equals(Base58Util.decode("1111111"), ByteArray(7,{0})))
            Base58Util.decode("93VYUMzRG9DdbRP72uQXjaWibbQwygnvaCu9DumcqDjGybD864T")
            shouldThrow<NoSuchElementException> {
                Base58Util.decode("This isn't valid base58")
            }
        }

        "Base58" should "decode encoded string" {
            for (i in 1 .. 1000) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
                val random = SecureRandom()
                random.setSeed( random.generateSeed(32) )

                val originalValue : kotlin.ByteArray = kotlin.ByteArray(32)
                assert(originalValue.size == 32)
                random.nextBytes(originalValue)

                val encodedValue = Base58Util.encode(originalValue)

                val decodedValue = Base58Util.decode(encodedValue)

                val encodedValue2 = Base58Util.encode(decodedValue)

                originalValue.toList() shouldBe decodedValue.toList()
                encodedValue.toList() shouldBe encodedValue2.toList()
            }
        }
    }
}