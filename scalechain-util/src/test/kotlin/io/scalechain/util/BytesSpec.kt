package io.scalechain.util
/*
import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import io.scalechain.test.TestMethods.S
import io.scalechain.test.TestMethods.A
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
            bytes.get(0) shouldBe 1.toByte()
            bytes.get(1) shouldBe 2.toByte()
            bytes.size shouldBe 2
        }

        "secondary constructor" should "accept a hex string" {
            val bytes = Bytes("0102")
            Arrays.equals(bytes.array.toTypedArray(), arrayOf<Byte>(1, 2)) shouldBe true
            bytes.get(0) shouldBe 1.toByte()
            bytes.get(1) shouldBe 2.toByte()
            bytes.size shouldBe 2
        }

        "hashCode" should "return a hash code for an empty array" {
            Bytes("").hashCode()
        }


        "hashCode" should "be equal if two Bytes have the same content " {
            val bytes1 = Bytes("0102")
            val bytes2 = Bytes("0102")
            bytes1.hashCode() shouldBe bytes2.hashCode()
        }

        "hashCode" should "not be equal if two Bytes have different contents" {
            val bytes1 = Bytes("0102")
            val bytes2 = Bytes("0103")
            (bytes1.hashCode() != bytes2.hashCode()) shouldBe true
        }

        "equals" should "return true if two Bytes have the same content" {
            val bytes1 = Bytes("")
            val bytes2 = Bytes("")
            bytes1.equals(bytes2) shouldBe true

            val bytes3 = Bytes("01")
            val bytes4 = Bytes("01")
            bytes3.equals(bytes4) shouldBe true

            val bytes5 = Bytes("0102")
            val bytes6 = Bytes("0102")
            bytes5.equals(bytes6) shouldBe true
        }

        "equals" should "return true for the same object" {
            val bytes1 = Bytes("0102")
            bytes1.equals(bytes1) shouldBe true
        }

        "equals" should "return false if the another is null" {
            Bytes("01").equals(null) shouldBe false
        }

        "equals" should "return false for two bytes with different length" {
            (Bytes("") == Bytes("01")) shouldBe false
            (Bytes("01") == Bytes("")) shouldBe false
            (Bytes("01") == Bytes("0101")) shouldBe false
            (Bytes("0101") == Bytes("01")) shouldBe false
        }


        "equlas" should "return false for different object type" {
            Bytes("0102").equals(1) shouldBe false
        }

        "get" should "should return the element" {
            val bytes = Bytes("0102")
            bytes.get(0) shouldBe 1.toByte()
            bytes.get(1) shouldBe 2.toByte()
            bytes[0] shouldBe 1.toByte()
            bytes[1] shouldBe 2.toByte()
        }

        "size" should "return size of the array" {
            Bytes("").size shouldBe 0
            Bytes("01").size shouldBe 1
            Bytes("0102").size shouldBe 2
        }

        "toString" should "return the string representation of the Bytes instance" {
            Bytes("").toString() shouldBe "Bytes(\"\")"
            Bytes("01").toString() shouldBe "Bytes(\"01\")"
            Bytes("0F").toString() shouldBe "Bytes(\"0f\")"
        }
    }
}
*/