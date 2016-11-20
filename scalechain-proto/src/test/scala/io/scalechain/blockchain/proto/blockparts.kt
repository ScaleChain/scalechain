package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import org.junit.Assert

class HashSpec : FlatSpec(), Matchers {

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
        "constructor" should "hit an assertion if the input hash array is empty" {
            shouldThrow<AssertionError> {
                Hash(Bytes(""))
            }
        }

        "isAllZero" should "return true if hash values are all zero" {
            Hash( Bytes("00") ).isAllZero() shouldBe true
            Hash( Bytes("0000") ).isAllZero() shouldBe true
        }

        "isAllZero" should "return false if there is any non-zero values in the hash" {
            Hash( Bytes("01") ).isAllZero() shouldBe false
            Hash( Bytes("0001") ).isAllZero() shouldBe false
            Hash( Bytes("0100") ).isAllZero() shouldBe false
        }

        "<" should "return true if the left one is less than the right one" {
            ( Hash( Bytes("00") ) < Hash( Bytes("01")) ) shouldBe true
            ( Hash( Bytes("01") ) < Hash( Bytes("02")) ) shouldBe true
            ( Hash( Bytes("01") ) < Hash( Bytes("0100")) ) shouldBe true
        }

        "<" should "return false if the left one is greater than the right one" {
            ( Hash( Bytes("01") ) < Hash( Bytes("00")) ) shouldBe false
            ( Hash( Bytes("02") ) < Hash( Bytes("01")) ) shouldBe false
            ( Hash( Bytes("0100") ) < Hash( Bytes("01")) ) shouldBe false
        }

        ">" should "return true if the left one is greater than the right one" {
            ( Hash( Bytes("01") ) > Hash( Bytes("00")) ) shouldBe true
            ( Hash( Bytes("02") ) > Hash( Bytes("01")) ) shouldBe true
            ( Hash( Bytes("0100") ) > Hash( Bytes("01")) ) shouldBe true
        }

        ">" should "return false if the left one is greater than the right one" {
            ( Hash( Bytes("00") ) > Hash( Bytes("01")) ) shouldBe false
            ( Hash( Bytes("01") ) > Hash( Bytes("02")) ) shouldBe false
            ( Hash( Bytes("01") ) > Hash( Bytes("0100")) ) shouldBe false
        }

        "==" should "return true for the hashes that have the same contents" {
            ( Hash( Bytes("01") ) == Hash( Bytes("01")) ) shouldBe true
            ( Hash( Bytes("0102") ) == Hash( Bytes("0102")) ) shouldBe true
        }

        "==" should "return false for the hashes that have the different contents" {
            ( Hash( Bytes("02") ) == Hash( Bytes("01")) ) shouldBe false
            ( Hash( Bytes("01") ) == Hash( Bytes("02")) ) shouldBe false
            ( Hash( Bytes("0102") ) == Hash( Bytes("0103")) ) shouldBe false
            ( Hash( Bytes("0103") ) == Hash( Bytes("0102")) ) shouldBe false
        }

        "Hash.ALL_ZERO.size" should "be 32" {
            Hash.ALL_ZERO.value.size shouldBe 32
        }

        "Hash.ALL_ZERO.size" should "have only zeros" {
            Hash.ALL_ZERO.isAllZero() shouldBe true
        }

    }
}