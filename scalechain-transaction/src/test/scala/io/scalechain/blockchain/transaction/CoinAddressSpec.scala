package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.TransactionOutput
import org.scalatest._
import io.scalechain.util.HexUtil._
/**
  * Created by kangmo on 5/18/16.
  */
class CoinAddressSpec extends FlatSpec with TransactionTestDataTrait with BeforeAndAfterEach with ShouldMatchers {

  this: Suite =>

  if (ChainEnvironmentFactory.getActive().isEmpty)
    ChainEnvironmentFactory.create("testnet")

  val env = ChainEnvironmentFactory.getActive().get

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(privateKey)" in {
    val privateKey = PrivateKey.generate
    val expectedAddress = CoinAddress.from(privateKey)
    val actualAddress = CoinAddress.from(expectedAddress.base58)

    actualAddress shouldBe expectedAddress
  }

  "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(publicKeyHash)" in {
    val privateKey = PrivateKey.generate
    val publicKey = PublicKey.from(privateKey)
    val expectedAddress = CoinAddress.from(publicKey.getHash.value)
    val actualAddress = CoinAddress.from(expectedAddress.base58)

    actualAddress shouldBe expectedAddress
  }


  "isValid" should "return true if it is valid" in {
    CoinAddress(env.PubkeyAddressVersion, bytes("0" * 40)).isValid shouldBe true
    CoinAddress(env.ScriptAddressVersion, bytes("0" * 40)).isValid shouldBe true

  }

  "isValid" should "return false if it is invalid" in {
    // invalid version
    CoinAddress('X', bytes("0" * 40)).isValid shouldBe false
    // invalid length
    CoinAddress(env.ScriptAddressVersion, bytes("0" * 38)).isValid shouldBe false
    // invalid length
    CoinAddress(env.ScriptAddressVersion, bytes("0" * 42)).isValid shouldBe false
  }

  "owns" should "return true for the transaction output it owns" in {
    val privateKey = PrivateKey.generate
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    val address = CoinAddress.from(privateKey)

    val output = TransactionOutput(1L, publicKeyScript.lockingScript())
    address.owns(output) shouldBe true

  }

  "owns" should "return false for the transaction output it does NOT own" in {
    val privateKey = PrivateKey.generate
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    // Generate an address from a new private key.
    val address = CoinAddress.from(PrivateKey.generate())

    val output = TransactionOutput(1L, publicKeyScript.lockingScript())
    address.owns(output) shouldBe false
  }

  "base58" should "return base58 representation of the address" in {
    val privateKey = PrivateKey.generate
    val address = CoinAddress.from(privateKey)
    address.base58.length should be > 0
  }

  "stringKey" should "return base58 representation of the address" in {
    val privateKey = PrivateKey.generate
    val address = CoinAddress.from(privateKey)
    address.stringKey shouldBe address.base58
  }

  /*
  "base58" should "return the coin address if it is valid" ignore {
    already tested by : "CoinAddress.from(address)" should "parse an encoded address using CoinAddress.from(privateKey)"
  }
  */

  "base58" should "hit an assertion if it is not valid" in {
    // invalid length
    val invalidAddress = CoinAddress(env.ScriptAddressVersion, bytes("0"*38))

    an [AssertionError] should be thrownBy invalidAddress.base58
  }
}
