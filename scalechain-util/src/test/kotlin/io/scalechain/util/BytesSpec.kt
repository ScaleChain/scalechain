package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import io.scalechain.test.TestMethods.S
import io.scalechain.test.TestMethods.A
import org.junit.runner.RunWith
import java.util.*

@RunWith(KTestJUnitRunner::class)
class BytesSpec : FlatSpec(), Matchers {

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
        "secondary constructor" should "accept a byte array" {
            val bytes = Bytes(A(1, 2))
            Arrays.equals(bytes.array.toTypedArray(), arrayOf<Byte>(1, 2)) shouldBe true
            bytes.array[0] shouldBe 1.toByte()
            bytes.array[1] shouldBe 2.toByte()
            bytes.array.size shouldBe 2
        }

        "hashCode" should "return a hash code for an empty array" {
            Bytes(A()).hashCode()
        }


        "hashCode" should "be equal if two Bytes have the same content " {
            val bytes1 = Bytes(A(1,2))
            val bytes2 = Bytes(A(1,2))
            bytes1.hashCode() shouldBe bytes2.hashCode()
        }

        "hashCode" should "not be equal if two Bytes have different contents" {
            val bytes1 = Bytes(A(1,2))
            val bytes2 = Bytes(A(1,3))
            (bytes1.hashCode() != bytes2.hashCode()) shouldBe true
        }

        "equals" should "return true if two Bytes have the same content" {
            val bytes1 = Bytes(A())
            val bytes2 = Bytes(A())
            bytes1.equals(bytes2) shouldBe true

            val bytes3 = Bytes(A(1))
            val bytes4 = Bytes(A(1))
            bytes3.equals(bytes4) shouldBe true

            val bytes5 = Bytes(A(1,2))
            val bytes6 = Bytes(A(1,2))
            bytes5.equals(bytes6) shouldBe true
        }

        "equals" should "return true for the same object" {
            val bytes1 = Bytes(A(1,2))
            bytes1.equals(bytes1) shouldBe true
        }

        "equals" should "return false if the another is null" {
            Bytes(A(1)).equals(null) shouldBe false
        }

        "equals" should "return false for two bytes with different length" {
            (Bytes(A()) == Bytes(A(1))) shouldBe false
            (Bytes(A(1)) == Bytes(A())) shouldBe false
            (Bytes(A(1)) == Bytes(A(1,1))) shouldBe false
            (Bytes(A(1,1)) == Bytes(A(1))) shouldBe false
        }


        "equlas" should "return false for different object type" {
            Bytes(A(1,2)).equals(1) shouldBe false
        }

        "get" should "should return the element" {
            val bytes = Bytes(A(1,2))
            bytes.array[0] shouldBe 1.toByte()
            bytes.array[1] shouldBe 2.toByte()
        }

        "size" should "return size of the array" {
            Bytes(A()).array.size shouldBe 0
            Bytes(A(1)).array.size shouldBe 1
            Bytes(A(1,2)).array.size shouldBe 2
        }

        "toString" should "return the string representation of the Bytes instance" {
            Bytes(A()).toString() shouldBe "Bytes(\"\")"
            Bytes(A(1)).toString() shouldBe "Bytes(\"01\")"
            Bytes(A(15)).toString() shouldBe "Bytes(\"0f\")"
        }
    }
}
