package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.ops.{OpPushData, OpEqual}
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
class ParsedPubKeyScriptSpec extends FlatSpec with BeforeAndAfterEach with TransactionTestDataTrait with ShouldMatchers {
  this: Suite =>

  if (ChainEnvironmentFactory.getActive().isEmpty)
    ChainEnvironmentFactory.create("testnet")

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

  "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method" in {
    val privateKey = PrivateKey.generate()
    val expectedPubKeyScript = ParsedPubKeyScript.from(privateKey)
    val actualPubKeyScript = ParsedPubKeyScript.from(expectedPubKeyScript.lockingScript)

    actualPubKeyScript shouldBe expectedPubKeyScript
  }

  /*
  "ParsedPubKeyScript.from(privateKey)" should "create a ParsedPubKeyScript" ignore {
    // Already tested by : "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method"

  }
  */


  "isValid" should "return true if it is created by ParsedPubKeyScript.from(privateKey)" in {
    val privateKey = PrivateKey.generate()
    val pubKeyScript = ParsedPubKeyScript.from(privateKey)
    pubKeyScript.isValid shouldBe true
  }

  "isValid" should "return true even though the script operations does not match standard patterns" in {
    ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A).isValid shouldBe true
  }

  "owns" should "return true for the transaction output it owns" in {
    val pubKeyScript = ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A)
    val output = TransactionOutput(1L, pubKeyScript.lockingScript())
    pubKeyScript.owns(output) shouldBe true
  }

  "owns" should "return false for the transaction output it does NOT own" in {
    val pubKeyScript1 = ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_A)
    val pubKeyScript2 = ParsedPubKeyScript(SIMPLE_SCRIPT_OPS_B)
    // pubKeyScript1 owns the output.
    val output = TransactionOutput(1L, pubKeyScript1.lockingScript())
    pubKeyScript2.owns(output) shouldBe false
  }

  "lockingScript" should "create a locking script that can be used in ParsedPubKeyScript.from to create the same ParsedPubKeyScript." ignore {
    // already tested by : "ParsedPubKeyScript.from(lockingScript)" should "parse a locking script created by lockingScript method"
  }
}
