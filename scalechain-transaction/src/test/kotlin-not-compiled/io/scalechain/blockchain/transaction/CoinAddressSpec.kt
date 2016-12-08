package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.TransactionOutput
import org.scalatest.*
import io.scalechain.util.HexUtil.bytes
/**
  * Created by kangmo on 5/18/16.
  */
class CoinAddressSpec : FlatSpec with TransactionTestDataTrait with BeforeAndAfterEach with ChainTestTrait with Matchers {

  this: Suite =>

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

  "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(privateKey)" {
    val privateKey = PrivateKey.generate
    val expectedAddress = CoinAddress.from(privateKey)
    val actualAddress = CoinAddress.from(expectedAddress.base58)

    actualAddress shouldBe expectedAddress
  }

  "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(publicKeyHash)" {
    val privateKey = PrivateKey.generate
    val publicKey = PublicKey.from(privateKey)
    val expectedAddress = CoinAddress.from(publicKey.getHash.value)
    val actualAddress = CoinAddress.from(expectedAddress.base58)

    actualAddress shouldBe expectedAddress
  }


  "isValid" should "return true if it is valid" {
    CoinAddress(env.PubkeyAddressVersion, bytes("0" * 40)).isValid shouldBe true
    CoinAddress(env.ScriptAddressVersion, bytes("0" * 40)).isValid shouldBe true

  }

  "isValid" should "return false if it is invalid" {
    // invalid version
    CoinAddress('X', bytes("0" * 40)).isValid shouldBe false
    // invalid length
    CoinAddress(env.ScriptAddressVersion, bytes("0" * 38)).isValid shouldBe false
    // invalid length
    CoinAddress(env.ScriptAddressVersion, bytes("0" * 42)).isValid shouldBe false
  }

  "base58" should "return base58 representation of the address" {
    val privateKey = PrivateKey.generate
    val address = CoinAddress.from(privateKey)
    address.base58.length should be > 0
  }

  "stringKey" should "return base58 representation of the address" {
    val privateKey = PrivateKey.generate
    val address = CoinAddress.from(privateKey)
    address.stringKey shouldBe address.base58
  }

  "lockingScript" should "produce locking script of the coin" {
    val privateKey = PrivateKey.generate
    val address = CoinAddress.from(privateKey)

    val expectedLockingScript = ParsedPubKeyScript.from(PublicKey.from(privateKey).getHash.bytes).lockingScript()

    address.lockingScript shouldBe expectedLockingScript

  }

  /*
  "base58" should "return the coin address if it is valid" ignore {
    already tested by : "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(privateKey)"
  }
  */

  "base58" should "hit an assertion if it is not valid" {
    // invalid length
    val invalidAddress = CoinAddress(env.ScriptAddressVersion, bytes("0"*38))

    an <AssertionError> should be thrownBy invalidAddress.base58
  }
}
