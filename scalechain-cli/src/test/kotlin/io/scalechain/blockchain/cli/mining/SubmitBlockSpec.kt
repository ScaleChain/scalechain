package io.scalechain.blockchain.cli.mining

import io.scalechain.blockchain.api.command.mining.SubmitBlock
import io.scalechain.blockchain.api.domain.{RpcError, StringResult}
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest._
import spray.json.{JsString, JsObject}

/**
  * Created by kangmo on 11/2/15.
  */

// The test does not pass yet. Will make it pass soon.
@Ignore
class SubmitBlockSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
  this: Suite =>

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

  // The test does not pass yet. Will make it pass soon.
  "SubmitBlock" should "get 'duplicate' for the duplicate block." in {
    val rawBlockData = JsString("02000000df11c014a8d798395b5059c722ebdf3171a4217ead71bf6e0e99f4c7000000004a6f6a2db225c81e77773f6f0457bcb05865a94900ed11356d0b75228efb38c7785d6053ffff001d005d43700101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0d03b477030164062f503253482fffffffff0100f9029500000000232103adb7d8ef6b63de74313e0cd4e07670d09a169b13e4eda2d650f529332c47646dac00000000")
    val parameters = JsObject( "workid" -> JsString("test"))
    val response = invoke(SubmitBlock, List(rawBlockData, parameters))
    val result = response.right.get.get.asInstanceOf[StringResult]
    result shouldBe StringResult("duplicate")
  }

  "SubmitBlock" should "return an error if no parameter was specified." in {
    val response = invoke(SubmitBlock)
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
  }
}
