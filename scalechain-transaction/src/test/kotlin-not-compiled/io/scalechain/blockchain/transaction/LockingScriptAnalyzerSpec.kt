package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.script.{ops, ScriptOpList, ScriptValue}
import io.scalechain.blockchain.script.ops.*
import org.scalatest.*

import io.scalechain.util.ByteArray
import ByteArray.*

class LockingScriptAnalyzerSpec : FlatSpec with BeforeAndAfterEach with TransactionTestDataTrait with ChainTestTrait with Matchers {

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

  "extractAddress(ScriptOpList)" should "extract an address from a pay to public key script list (p2pk)" {
    val privateKey = PrivateKey.generate
    val publicKey = PublicKey.from(privateKey)
    val encodedPublicKey = publicKey.encode()
    val p2pkScript = ScriptOpList( listOf(
                        OpPush(encodedPublicKey.length, ScriptValue.valueOf(encodedPublicKey)),
                        OpCheckSig()) )

    val expectedAddress  = CoinAddress.from(privateKey)

    val extractedAdddress = LockingScriptAnalyzer.extractAddresses(p2pkScript)

    extractedAdddress shouldBe listOf(expectedAddress)
  }

  "extractAddress(ScriptOpList)" should "extract an address from a pay to public key script list (p2pkh)" {
    val privateKey = PrivateKey.generate
    val publicKey = PublicKey.from(privateKey)
    val publicKeyHash = publicKey.getHash()
    val p2pkScript = ScriptOpList( listOf(
                         OpDup(),
                         OpHash160(),
                         OpPush(publicKeyHash.value.length, ScriptValue.valueOf(publicKeyHash.value)),
                         OpEqualVerify(),
                         OpCheckSig()
    ))

    val expectedAddress  = CoinAddress.from(privateKey)

    val extractedAdddress = LockingScriptAnalyzer.extractAddresses(p2pkScript)

    extractedAdddress shouldBe listOf(expectedAddress)
  }

  "extractAddress(ScriptOpList)" should "extract an address from a ParsedPubKeyScript" {
    val privateKey = PrivateKey.generate
    val pubKeyScript = ParsedPubKeyScript.from(privateKey)

    val expectedAddress  = CoinAddress.from(privateKey)

    val extractedAdddress = LockingScriptAnalyzer.extractAddresses(pubKeyScript.scriptOps)

    extractedAdddress shouldBe listOf(expectedAddress)

  }

  "extractAddresses(LockingScript)" should "extract an address from a locking script of pay to public key hash" {
    val privateKey = PrivateKey.generate
    val pubKeyScript = ParsedPubKeyScript.from(privateKey)

    val expectedAddress  = CoinAddress.from(privateKey)
    val extractedAdddress = LockingScriptAnalyzer.extractAddresses(pubKeyScript.lockingScript())

    extractedAdddress shouldBe listOf(expectedAddress)
  }

  "extractAddresses(LockingScript)" should "extract multiple addresses for multisig script" ignore {
    // TODO : Implement
  }


  "extractOutputOwnership" should "extract a coin address" {
    val privateKey = PrivateKey.generate
    val pubKeyScript = ParsedPubKeyScript.from(privateKey)

    val expectedAddress  = CoinAddress.from(privateKey)
    val extractedOwnership = LockingScriptAnalyzer.extractOutputOwnership(pubKeyScript.lockingScript())

    extractedOwnership shouldBe expectedAddress
  }

  "extractOutputOwnership" should "extract a parsed public key script " {
    // A simple locking script asking to provide any unlocking script that results in value 5 on the value stack.
    val scriptOps = ScriptOpList( listOf(
      OpNum(5), OpEqual()
    ))

    val expectedOwnership = ParsedPubKeyScript(scriptOps)

    val extractedOwnership = LockingScriptAnalyzer.extractOutputOwnership(expectedOwnership.lockingScript())

    extractedOwnership shouldBe expectedOwnership
  }

}

