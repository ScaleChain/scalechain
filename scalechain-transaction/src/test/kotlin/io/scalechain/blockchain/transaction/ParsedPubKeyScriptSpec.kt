package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.ScriptSerializer
import io.scalechain.blockchain.script.ops.OpCheckSig
import io.scalechain.blockchain.script.ops.OpPushData
import io.scalechain.blockchain.script.ops.OpEqual
import io.scalechain.blockchain.transaction.TransactionTestData.SIMPLE_SCRIPT_OPS_A
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

/**
  * Created by kangmo on 5/18/16.
  */
@RunWith(KTestJUnitRunner::class)
class ParsedPubKeyScriptSpec : FlatSpec(), Matchers, TransactionTestInterface, ChainTestTrait {

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
    "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method" {
      val privateKey = PrivateKey.generate()
      val expectedPubKeyScript = ParsedPubKeyScript.from(privateKey)
      val actualPubKeyScript = ParsedPubKeyScript.from(expectedPubKeyScript.lockingScript())

      val checksigScriptScrubbed = scrubScript(actualPubKeyScript)

      checksigScriptScrubbed shouldBe expectedPubKeyScript
    }

    "ParsedPubKeyScript.from(pubKeyHash)" should "create a ParsedPubKeyScript from the public key hash" {
      val privateKey = PrivateKey.generate()
      val expectedPubKeyScript = ParsedPubKeyScript.from(privateKey)
      val publicKey = PublicKey.from(privateKey)

      val actualPubKeyScript = ParsedPubKeyScript.from(publicKey.getHash().value.array)

      val checksigScriptScrubbed = scrubScript(actualPubKeyScript)

      checksigScriptScrubbed shouldBe expectedPubKeyScript
    }

    /*
    "ParsedPubKeyScript.from(privateKey)" should "create a ParsedPubKeyScript" ignore {
      // Already tested by : "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method"

    }
    */


    "isValid" should "return true if it is created by ParsedPubKeyScript.from(privateKey)" {
      val privateKey = PrivateKey.generate()
      val pubKeyScript = ParsedPubKeyScript.from(privateKey)
      pubKeyScript.isValid() shouldBe true
    }

    "isValid" should "return true even though the script operations does not match standard patterns" {
      ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A).isValid() shouldBe true
    }

    "stringKey" should "return hex representation of the locking script" {
      val pubKeyScript = ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A)
      pubKeyScript.stringKey() shouldBe HexUtil.hex(pubKeyScript.lockingScript().data.array)
    }

    /*
    "lockingScript" should "create a locking script that can be used in ParsedPubKeyScript.from to create the same ParsedPubKeyScript." {
      // already tested by : "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method"
    }
    */
  }
}
