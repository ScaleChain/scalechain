package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import io.scalechain.test.TestMethods.S
import io.scalechain.test.TestMethods.A
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class HexUtilSpec : FlatSpec(), Matchers {
    fun byteArrayOf(vararg args : Int) : ByteArray = args.map { it.toByte() }.toByteArray()

    init {
        "bytes" should "convert a hex string to a byte array" {
            HexUtil.bytes( S("") ).toList() shouldBe listOf<Byte>()

            HexUtil.bytes( S("01") ).toList() shouldBe byteArrayOf(1).toList()

            HexUtil.bytes( S("0a") ).toList() shouldBe byteArrayOf(10).toList()

            HexUtil.bytes( S("01 02") ).toList() shouldBe byteArrayOf(1, 2).toList()

            HexUtil.bytes( S("01 02 03") ).toList() shouldBe byteArrayOf(1, 2, 3).toList()
        }

        "bytes" should "convert a hex string to a byte array even though the effective bytes in hex string is odd" {
            HexUtil.bytes( S("1") ).toList() shouldBe byteArrayOf(1).toList()
            HexUtil.bytes( S("001") ).toList() shouldBe byteArrayOf(0, 1).toList()
        }


        "hex" should "convert a byte array to a hex" {
            HexUtil.hex(A()) shouldBe ""
            HexUtil.hex(A(1)) shouldBe "01"
            HexUtil.hex(A(10)) shouldBe "0a"
            HexUtil.hex(A(1,2)) shouldBe "0102"
            HexUtil.hex(A(1,2,3)) shouldBe "010203"
            HexUtil.hex(A(1,2,3), ",") shouldBe "01,02,03"
        }

        "prettyHex" should "convery a byte array to a pretty hex string" {
            HexUtil.prettyHex(A()) shouldBe ""
            HexUtil.prettyHex(A(1)) shouldBe "01"
            HexUtil.prettyHex(A(10)) shouldBe "0a"
            HexUtil.prettyHex(A(1,2)) shouldBe "01 02"
            HexUtil.prettyHex(A(1,2,3)) shouldBe "01 02 03"
        }

        "kotlinHex" should "convert a byte array to a hex string in a format that can be used as kotlin source code" {
            HexUtil.kotlinHex(A()) shouldBe "\"\""
            HexUtil.kotlinHex(A(1)) shouldBe  "\"01\""
            HexUtil.kotlinHex(A(10)) shouldBe  "\"0a\""
            HexUtil.kotlinHex(A(1,2)) shouldBe  "\"0102\""
            HexUtil.kotlinHex(A(1,2,3)) shouldBe  "\"010203\""
        }
    }
}
