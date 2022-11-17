package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.test.TestMethods.filledString
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
  * Created by kangmo on 5/18/16.
  */
@RunWith(KTestJUnitRunner::class)
class CoinAddressSpec : FlatSpec(), Matchers, TransactionTestInterface, ChainTestTrait {

  override fun beforeEach() {
    super.beforeEach()

    // Create environment
    env()
  }

  override fun afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  init {
    "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(privateKey)" {
      val privateKey = PrivateKey.generate()
      val expectedAddress = CoinAddress.from(privateKey)
      val actualAddress = CoinAddress.from(expectedAddress.base58())

      actualAddress shouldBe expectedAddress
    }

    "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(publicKeyHash)" {
      val privateKey = PrivateKey.generate()
      val publicKey = PublicKey.from(privateKey)
      val expectedAddress = CoinAddress.from(publicKey.getHash().value.array)
      val actualAddress = CoinAddress.from(expectedAddress.base58())

      actualAddress shouldBe expectedAddress
    }


    "isValid" should "return true if it is valid" {
      CoinAddress(env().PubkeyAddressVersion, Bytes.from(filledString(40, '0'.code.toByte()))).isValid() shouldBe true
      CoinAddress(env().ScriptAddressVersion, Bytes.from(filledString(40, '0'.code.toByte()))).isValid() shouldBe true

    }

    "isValid" should "return false if it is invalid" {
      // invalid version
      CoinAddress('X'.code.toByte(), Bytes.from(filledString(40, '0'.code.toByte()))).isValid() shouldBe false
      // invalid length
      CoinAddress(env().ScriptAddressVersion, Bytes.from(filledString(38, '0'.code.toByte()))).isValid() shouldBe false
      // invalid length
      CoinAddress(env().ScriptAddressVersion, Bytes.from(filledString(42, '0'.code.toByte()))).isValid() shouldBe false
    }

    "base58" should "return base58 representation of the address" {
      val privateKey = PrivateKey.generate()
      val address = CoinAddress.from(privateKey)
      (address.base58().length > 0) shouldBe true
    }

    "stringKey" should "return base58 representation of the address" {
      val privateKey = PrivateKey.generate()
      val address = CoinAddress.from(privateKey)
      address.stringKey() shouldBe address.base58()
    }

    "lockingScript" should "produce locking script of the coin" {
      val privateKey = PrivateKey.generate()
      val address = CoinAddress.from(privateKey)

      val expectedLockingScript = ParsedPubKeyScript.from(PublicKey.from(privateKey).getHash().value.array).lockingScript()

      address.lockingScript() shouldBe expectedLockingScript

    }

    /*
    "base58" should "return the coin address if it is valid" ignore {
      already tested by : "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(privateKey)"
    }
    */

    "base58" should "hit an assertion if it is not valid" {
      // invalid length
      val invalidAddress = CoinAddress(env().ScriptAddressVersion, Bytes.from(filledString(38, '0'.code.toByte())))

      shouldThrow <AssertionError> {
        invalidAddress.base58()
      }
    }
  }
}
