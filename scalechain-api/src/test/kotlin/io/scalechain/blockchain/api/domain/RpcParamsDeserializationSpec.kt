package io.scalechain.blockchain.api.domain

import org.scalatest._
import spray.json.DefaultJsonProtocol._
import spray.json._

class RpcParamsDeserializationSpec extends FlatSpec with BeforeAndAfterEach with Matchers {
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

  import RpcParamsJsonFormat._

  implicit val implicitJsonRpcRequest = jsonFormat4(RpcRequest.apply)

  "RpcParams" should "be able to leave list of JsValues as is, if we want" in {

    val jsonrpcValue = "1.0"
    val id = 1
    val method = "mymethod"
    val params = JsArray(
      // a simple string
      JsString("1"),
      // a simple number
      JsNumber(1.5),
      // a simple array
      JsArray(
        JsString("2"),
        JsNumber(3.5)
      ),
      // an object with an array field
      JsObject(
        "field" -> JsString("3"),
        "arr" -> JsArray(
          JsString("4")
        )
      ),
      // an array of objects, which may have an array of values
      JsArray(
        JsObject(
          "field" -> JsString("5"),
          "arr" -> JsArray(
            JsString("6")
          )
        ),
        JsObject(
          "field" -> JsNumber(4.5)
        )
      )
    )

    val jsObject = JsObject(
      "jsonrpc" -> JsString(jsonrpcValue),
      "id" -> JsNumber(id),
      "method" -> JsString(method),
      "params" -> params
      )

    val request = jsObject.convertTo[RpcRequest]

    request.jsonrpc shouldBe Some(jsonrpcValue)
    request.id shouldBe id
    request.method shouldBe method
    request.params.paramValues shouldBe params.elements.toList
  }

  "RpcParams" should "not throw a DeserializationException even though jsonrpc field is missing " in {
    val jsObject = JsObject(
      //"jsonrpc" -> JsString("1.0"),
      "id" -> JsNumber(1),
      "method" -> JsString("myMethod"),
      "params" -> JsArray( JsString("arg1") )
    )

    jsObject.convertTo[RpcRequest]
  }

  "RpcParams" should "throw DeserializationException if id field is missing " in {
    val jsObject = JsObject(
      "jsonrpc" -> JsString("1.0"),
      //"id" -> JsString("abc"),
      "method" -> JsString("myMethod"),
      "params" -> JsArray( JsString("arg1") )
    )

    the [spray.json.DeserializationException] thrownBy {
      jsObject.convertTo[RpcRequest]
    } should have message "Object is missing required member 'id'"
  }

  "RpcParams" should "throw DeserializationException if method field is missing " in {
    val jsObject = JsObject(
      "jsonrpc" -> JsString("1.0"),
      "id" -> JsNumber(1),
      //"method" -> JsString("myMethod"),
      "params" -> JsArray( JsString("arg1") )
    )

    the [spray.json.DeserializationException] thrownBy {
      jsObject.convertTo[RpcRequest]
    } should have message "Object is missing required member 'method'"
  }

  "RpcParams" should "throw DeserializationException if params field is missing " in {
    val jsObject = JsObject(
      "jsonrpc" -> JsString("1.0"),
      "id" -> JsNumber(1),
      "method" -> JsString("myMethod")
      //"params" -> JsArray( JsString("arg1") )
    )

    the [spray.json.DeserializationException] thrownBy {
      jsObject.convertTo[RpcRequest]
    } should have message "Object is missing required member 'params'"
  }

  "RpcParams" should "throw DeserializationException if params field is not an array " in {
    val jsObject = JsObject(
      "jsonrpc" -> JsString("1.0"),
      "id" -> JsNumber(1),
      "method" -> JsString("myMethod"),
      "params" -> JsString("arg1")
    )

    the [spray.json.DeserializationException] thrownBy {
      jsObject.convertTo[RpcRequest]
    } should have message "JsArray expected for the params field."
  }

}
