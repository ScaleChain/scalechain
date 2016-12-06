package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith


// Test case for interface Script
@RunWith(KTestJUnitRunner::class)
class ScriptSpec : FlatSpec(), Matchers {
    override fun beforeEach() {
        // Need to create a concrete class that implements Script Interface

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

    }

    fun script(byteArray : ByteArray) = object : Script { override val data = byteArray}

    init {
        "size" should "return the size of the array in the script" {
            script(byteArrayOf()).size() shouldBe 0
            script(byteArrayOf(0)).size() shouldBe 1
            script(byteArrayOf(1)).size() shouldBe 1
            script(byteArrayOf(0,1)).size() shouldBe 2
            script(byteArrayOf(0,1,2)).size() shouldBe 3
        }

        "get" should "be able to get a value in the script array" {
            script(byteArrayOf(0)).get(0) shouldBe 0.toByte()
            script(byteArrayOf(1)).get(0) shouldBe 1.toByte()
            script(byteArrayOf(0,1)).get(0) shouldBe 0.toByte()
            script(byteArrayOf(0,1)).get(1) shouldBe 1.toByte()
            script(byteArrayOf(0,1,2)).get(0) shouldBe 0.toByte()
            script(byteArrayOf(0,1,2)).get(1) shouldBe 1.toByte()
            script(byteArrayOf(0,1,2)).get(2) shouldBe 2.toByte()
        }

        "get" should "throw ArrayOutOfBounds exception" {
            shouldThrow<ArrayIndexOutOfBoundsException> {
                script(byteArrayOf()).get(0)
            }

            shouldThrow<ArrayIndexOutOfBoundsException> {
                script(byteArrayOf()).get(1)
            }

            shouldThrow<ArrayIndexOutOfBoundsException> {
                script(byteArrayOf(0)).get(1)
            }

            shouldThrow<ArrayIndexOutOfBoundsException> {
                script(byteArrayOf(0,1)).get(2)
            }

        }

    }
}