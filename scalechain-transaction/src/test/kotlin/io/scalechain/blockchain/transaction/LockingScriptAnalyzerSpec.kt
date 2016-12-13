package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ops.*
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class LockingScriptAnalyzerSpec : FlatSpec(), Matchers, TransactionTestDataTrait, ChainTestTrait {

  init {
    "extractAddress(ScriptOpList)" should "extract an address from a pay to public key script list (p2pk)" {
      val privateKey = PrivateKey.generate()
      val publicKey = PublicKey.from(privateKey)
      val encodedPublicKey = publicKey.encode()
      val p2pkScript = ScriptOpList(listOf(
          OpPush(encodedPublicKey.size, ScriptValue.valueOf(encodedPublicKey)),
          OpCheckSig()))

      val expectedAddress = CoinAddress.from(privateKey)

      val extractedAdddress = LockingScriptAnalyzer.extractAddresses(p2pkScript)

      extractedAdddress shouldBe listOf(expectedAddress)
    }

    "extractAddress(ScriptOpList)" should "extract an address from a pay to public key script list (p2pkh)" {
      val privateKey = PrivateKey.generate()
      val publicKey = PublicKey.from(privateKey)
      val publicKeyHash = publicKey.getHash()
      val p2pkScript = ScriptOpList(listOf(
          OpDup(),
          OpHash160(),
          OpPush(publicKeyHash.value.size, ScriptValue.valueOf(publicKeyHash.value)),
          OpEqualVerify(),
          OpCheckSig()
      ))

      val expectedAddress = CoinAddress.from(privateKey)

      val extractedAdddress = LockingScriptAnalyzer.extractAddresses(p2pkScript)

      extractedAdddress shouldBe listOf(expectedAddress)
    }

    "extractAddress(ScriptOpList)" should "extract an address from a ParsedPubKeyScript" {
      val privateKey = PrivateKey.generate()
      val pubKeyScript = ParsedPubKeyScript.from(privateKey)

      val expectedAddress = CoinAddress.from(privateKey)

      val extractedAdddress = LockingScriptAnalyzer.extractAddresses(pubKeyScript.scriptOps)

      extractedAdddress shouldBe listOf(expectedAddress)

    }

    "extractAddresses(LockingScript)" should "extract an address from a locking script of pay to public key hash" {
      val privateKey = PrivateKey.generate()
      val pubKeyScript = ParsedPubKeyScript.from(privateKey)

      val expectedAddress = CoinAddress.from(privateKey)
      val extractedAdddress = LockingScriptAnalyzer.extractAddresses(pubKeyScript.lockingScript())

      extractedAdddress shouldBe listOf(expectedAddress)
    }

    /*
    "extractAddresses(LockingScript)" should "extract multiple addresses for multisig script" {
      // TODO : Implement
    }*/


    "extractOutputOwnership" should "extract a coin address" {
      val privateKey = PrivateKey.generate()
      val pubKeyScript = ParsedPubKeyScript.from(privateKey)

      val expectedAddress = CoinAddress.from(privateKey)
      val extractedOwnership = LockingScriptAnalyzer.extractOutputOwnership(pubKeyScript.lockingScript())

      extractedOwnership shouldBe expectedAddress
    }

    "extractOutputOwnership" should "extract a parsed public key script " {
      // A simple locking script asking to provide any unlocking script that results in value 5 on the value stack.
      val scriptOps = ScriptOpList(listOf(
          OpNum(5), OpEqual()
      ))

      val expectedOwnership = ParsedPubKeyScript(scriptOps)

      val extractedOwnership = LockingScriptAnalyzer.extractOutputOwnership(expectedOwnership.lockingScript())

      extractedOwnership shouldBe expectedOwnership
    }
  }
}

