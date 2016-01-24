package io.scalechain.blockchain.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import spray.json._


// domain model
/*
final case class Item(name: String, id: Long)
final case class Order(items: List[Item])
*/
// by kangmo
final case class TestResponse(message : String)

trait JsonRpcResult

case class JsonRpcRequest(jsonrpc:String, id:String, method:String, params:Array[String])

// TODO : Make sure the format matches the one used by Bitcoin.
case class JsonRpcError(code : Int, message : String, data : String)
object JsonRpcError {
  case class ErrorDescription( code : Int, message : String)
  val METHOD_NOT_FOUND = ErrorDescription(1000, "Method not found")
}


case class JsonRpcResponse(result : Option[JsonRpcResult], error:Option[JsonRpcError], id:String)

trait JsonRpcService {
  def invoke(request : JsonRpcRequest) : JsonRpcResult
}


/*
// Json-RPC request : {"jsonrpc": "1.0", "id":"curltest", "method": "getinfo", "params": [] }
// Json-RPC response :
{
  "result": {
    "version": 110100,
    "protocolversion": 70002,
    "walletversion": 60000,
    "balance": 0,
    "blocks": 394722,
    "timeoffset": -24,
    "connections": 8,
    "proxy": "",
    "difficulty": 113354299801.47,
    "testnet": false,
    "keypoololdest": 1445528771,
    "keypoolsize": 101,
    "paytxfee": 0,
    "relayfee": 5.0e-5,
    "errors": ""
  },
  "error": null,
  "id": "curltest"
}
*/

case class GetInfoResponse(
  version : Int,
  protocolversion : Int,
  walletversion : Int,
  balance: Int,
  blocks: Int,
  timeoffset: Int,
  connections : Int,
  proxy: String,
  difficulty: scala.math.BigDecimal,
  testnet: Boolean,
  keypoololdest: Long,
  keypoolsize: Int,
  paytxfee : Int,
  // Make sure the Json serialized format is like "5.0e-5"
  relayfee: scala.math.BigDecimal,
  errors: String
) extends JsonRpcResult

object GetInfo extends JsonRpcService {

  def invoke(request : JsonRpcRequest) : JsonRpcResult = {
    GetInfoResponse(
      version = 110100,
      protocolversion = 70002,
      walletversion = 60000,
      balance = 0,
      blocks = 394722,
      timeoffset = -24,
      connections = 8,
      proxy = "",
      difficulty = new java.math.BigDecimal(113354299801.47),
      testnet = false,
      keypoololdest = 1445528771,
      keypoolsize = 101,
      paytxfee = 0,
      // Make sure the Json serialized format is like "5.0e-5"
      relayfee = new java.math.BigDecimal(5.0e-5),
      errors = ""
    )
  }
}

trait ServiceDispatcher {
  // A map from Json-Rpc method to the actual JsonRpcService object that handles it.
  val serviceMap = Map[String, JsonRpcService]( "getinfo" -> GetInfo )
  def dispatch(request : JsonRpcRequest) : JsonRpcResponse = {

    val methodName = request.method
    val serviceOption = serviceMap.get(methodName)
    if (serviceOption.isDefined) {
      val serviceResult = serviceOption.get.invoke(request)
      JsonRpcResponse(
        result = Some(serviceResult),
        error = None,
        id = request.id
      )
    } else {
      JsonRpcResponse(
        result = None,
        error = Some(JsonRpcError(
                  code = JsonRpcError.METHOD_NOT_FOUND.code,
                  message = JsonRpcError.METHOD_NOT_FOUND.message,
                  data = methodName
                )),
        id = request.id
      )
    }
  }
}

case class Test(a : String)

// use it wherever json (un)marshalling is needed
trait JsonRpc extends Directives with SprayJsonSupport with DefaultJsonProtocol with ServiceDispatcher {
  implicit object JsonRpcResultFormat extends RootJsonFormat[JsonRpcResult] {
    case class Dummy() extends JsonRpcResult
    def write(result : JsonRpcResult) = result match {
      case r : GetInfoResponse => r.toJson
    }
    def read(value:JsValue) = Dummy()
  }

  implicit val implcitJsonRpcError = jsonFormat3(JsonRpcError.apply)
  implicit val implcitJsonRpcRequest = jsonFormat4(JsonRpcRequest.apply)
  implicit val implcitJsonRpcResponse = jsonFormat3(JsonRpcResponse.apply)
  implicit val implcitGetInfoResponse = jsonFormat15(GetInfoResponse.apply)


  implicit val implcitTest = jsonFormat1(Test.apply)

  // format: OFF
  val routes = {
    pathPrefix("") {

      (post & entity(as[JsonRpcRequest])) { request =>
        complete {
          val serviceResponse = dispatch(request)
          serviceResponse
        }
      }
    }
  }
}

// BUGBUG :
// Currently, only the following is accepted for the content-type HTTP header.
//   Content-Type: application/json
// followings should be accepted :
//   Content-Type: application/json;
//   Content-Type: plain/text
//   Content-Type: plain/text;
object JsonRpcMicroservice extends App with JsonRpc {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  Console.readLine() // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.shutdown()) // and shutdown when done
}

