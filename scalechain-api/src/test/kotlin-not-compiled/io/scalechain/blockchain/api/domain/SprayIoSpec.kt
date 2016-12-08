package io.scalechain.blockchain.api.domain

import org.scalatest.*
import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.*

/**
  * Test cases for validating our assumptions on spray.io
  */
class SprayIoSpec : FlatSpec with BeforeAndAfterEach with Matchers {
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

  implicit object JsValueFormat : RootJsonFormat<JsValue> {
    fun write( any : JsValue ) {
      // Not used.
      assert(false);
      "".toJson
    }

    fun read( value: JsValue ) : JsValue {
      value
    }
  }

  "convertTo" should "be able to leave JsValue as is if we want" {
    val jsStr = JsString("abc")
    jsStr.convertTo<JsValue> shouldBe jsStr
  }
}
