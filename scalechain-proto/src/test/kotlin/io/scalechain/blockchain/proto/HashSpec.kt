package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.ByteBufExt
import io.scalechain.util.Bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class HashSpec : FlatSpec(), Matchers {

    fun B(hexString : String) = Bytes.from(hexString)

    init {
        "constructor" should "hit an assertion if the input hash array is empty" {
            shouldThrow<AssertionError> {
                Hash(B(""))
            }
        }

        "isAllZero" should "return true if hash values are all zero" {
            Hash( B("00") ).isAllZero() shouldBe true
            Hash( B("0000") ).isAllZero() shouldBe true
        }

        "isAllZero" should "return false if there is any non-zero values in the hash" {
            Hash( B("01") ).isAllZero() shouldBe false
            Hash( B("0001") ).isAllZero() shouldBe false
            Hash( B("0100") ).isAllZero() shouldBe false
        }

        "<" should "return true if the left one is less than the right one" {
            ( Hash( B("00") ) < Hash( B("01")) ) shouldBe true
            ( Hash( B("01") ) < Hash( B("02")) ) shouldBe true
            ( Hash( B("01") ) < Hash( B("0100")) ) shouldBe true
        }

        "<" should "return false if the left one is greater than the right one" {
            ( Hash( B("01") ) < Hash( B("00")) ) shouldBe false
            ( Hash( B("02") ) < Hash( B("01")) ) shouldBe false
            ( Hash( B("0100") ) < Hash( B("01")) ) shouldBe false
        }

        ">" should "return true if the left one is greater than the right one" {
            ( Hash( B("01") ) > Hash( B("00")) ) shouldBe true
            ( Hash( B("02") ) > Hash( B("01")) ) shouldBe true
            ( Hash( B("0100") ) > Hash( B("01")) ) shouldBe true
        }

        ">" should "return false if the left one is greater than the right one" {
            ( Hash( B("00") ) > Hash( B("01")) ) shouldBe false
            ( Hash( B("01") ) > Hash( B("02")) ) shouldBe false
            ( Hash( B("01") ) > Hash( B("0100")) ) shouldBe false
        }

        "==" should "return true for the hashes that have the same contents" {
            ( Hash( B("01") ) == Hash( B("01")) ) shouldBe true
            ( Hash( B("0102") ) == Hash( B("0102")) ) shouldBe true
        }

        "==" should "return false for the hashes that have the different contents" {
            ( Hash( B("02") ) == Hash( B("01")) ) shouldBe false
            ( Hash( B("01") ) == Hash( B("02")) ) shouldBe false
            ( Hash( B("0102") ) == Hash( B("0103")) ) shouldBe false
            ( Hash( B("0103") ) == Hash( B("0102")) ) shouldBe false
        }

        "Hash.ALL_ZERO.size" should "be 32" {
            Hash.ALL_ZERO.value.array.size shouldBe 32
        }

        "Hash.ALL_ZERO.size" should "have only zeros" {
            Hash.ALL_ZERO.isAllZero() shouldBe true
        }

    }
}