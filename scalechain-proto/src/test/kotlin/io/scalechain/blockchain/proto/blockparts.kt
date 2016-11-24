package io.scalechain.blockchain.proto

import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.ByteBufExt
import org.junit.Assert

class HashSpec : FlatSpec(), Matchers {

    fun B(hexString : String) = ByteBufExt.from(hexString)

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
            Hash.ALL_ZERO.value.capacity() shouldBe 32
        }

        "Hash.ALL_ZERO.size" should "have only zeros" {
            Hash.ALL_ZERO.isAllZero() shouldBe true
        }

    }
}


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

        }

        "isCoinBaseInput" should "return true if bytes in the outputTransactionHash is all zero" {

        }
    }
}

// Test case for interface Script
class ScriptSpec : FlatSpec(), Matchers {
    override fun beforeEach() {
        // Need to create a concrete class that implements Script Interface

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

    }

    init {
        "size" should "return the size of the array in the script" {

        }

        "get" should "be able to get the first value of the script array" {

        }

        "get" should "be able to get the second value of the script array" {

        }
    }
}

// Test case for data class LockingScript
class LockingScriptSpec : FlatSpec(), Matchers {
    init {
        "toString" should "return default string representation if the printer is not set" {

        }

        "toString" should "return the printer's result if the printer is set" {

            // Need to revert the printer to null at the end of this case
        }
    }
}

// Test case for data class UnlockingScript
class UnlockingScriptSpec : FlatSpec(), Matchers {
    init {
        "toString" should "return default string representation if the printer is not set" {

        }

        "toString" should "return the printer's result if the printer is set" {

            // Need to revert the printer to null at the end of this case
        }
    }
}


// Test case for data class Transaction
class TransactionSpec : FlatSpec(), Matchers {
    init {
        "toString" should "return default string representation if the printer is not set" {

        }

        "toString" should "return the printer's result if the printer is set" {
            // Need to revert the printer to null at the end of this case
        }
    }
}

// Test case for data class IPv6Address
class IPv6AddressSpec : FlatSpec(), Matchers {
    init {
        "inetAddress" should "return InetAddress by reading big endian encoding of the adress in the address property." {

        }
    }
}