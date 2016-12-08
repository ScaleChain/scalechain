package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.ops.{OpCheckSig, OpPushData, OpEqual}
import io.scalechain.util.HexUtil
import org.scalatest.*
import sun.font.Script

/**
  * Created by kangmo on 5/18/16.
  */
class ParsedPubKeyScriptSpec : FlatSpec with BeforeAndAfterEach with TransactionTestDataTrait with ChainTestTrait with Matchers {
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

  "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method" {
    val privateKey = PrivateKey.generate()
    val expectedPubKeyScript = ParsedPubKeyScript.from(privateKey)
    val actualPubKeyScript = ParsedPubKeyScript.from(expectedPubKeyScript.lockingScript)

    val checksigScriptScrubbed = scrubScript(actualPubKeyScript)

    checksigScriptScrubbed shouldBe expectedPubKeyScript
  }

  "ParsedPubKeyScript.from(pubKeyHash)" should "create a ParsedPubKeyScript from the public key hash" {
    val privateKey = PrivateKey.generate()
    val expectedPubKeyScript = ParsedPubKeyScript.from(privateKey)
    val publicKey = PublicKey.from(privateKey)

    val actualPubKeyScript = ParsedPubKeyScript.from(publicKey.getHash().value)

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
    pubKeyScript.isValid shouldBe true
  }

  "isValid" should "return true even though the script operations does not match standard patterns" {
    ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A).isValid shouldBe true
  }

  "stringKey" should "return hex representation of the locking script" {
    val pubKeyScript = ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A)
    pubKeyScript.stringKey shouldBe HexUtil.hex(pubKeyScript.lockingScript().data)
  }

  "lockingScript" should "create a locking script that can be used in ParsedPubKeyScript.from to create the same ParsedPubKeyScript." ignore {
    // already tested by : "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method"
  }
}
