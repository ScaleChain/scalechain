package io.scalechain.blockchain.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import io.scalechain.blockchain.api.command.blockchain.GetTxOutResult
import io.scalechain.blockchain.api.command.blockchain.p1.{GetTxOutResult, ScriptPubKey}
import io.scalechain.blockchain.api.command.control.GetInfoResult
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcResponse, RpcRequest, RpcResult}
import spray.json._
import io.scalechain.util.Config



// use it wherever json (un)marshalling is needed
trait JsonRpc extends Directives with SprayJsonSupport with DefaultJsonProtocol with ServiceDispatcher {
/*
  implicit object ScalaValueFormat extends RootJsonFormat[AnyRef] {
    def write(value : AnyRef) = value.toJson
    def read(value :JsValue) = ""
  }
*/
  implicit object JsonRpcResultFormat extends RootJsonFormat[RpcResult] {
    case class Dummy() extends RpcResult
    def write(result : RpcResult) = result match {
      case r : GetInfoResult => r.toJson
      case r : GetTxOutResult => r.toJson
    }
    def read(value:JsValue) = Dummy()
  }

  implicit val implcitJsonRpcError = jsonFormat3(RpcError.apply)
  implicit val implcitJsonRpcRequest = jsonFormat4(RpcRequest.apply)
  implicit val implcitJsonRpcResponse = jsonFormat3(RpcResponse.apply)

  implicit val implcitGetInfoResult = jsonFormat15(GetInfoResult.apply)

  implicit val implcitScriptPubKey = jsonFormat5(ScriptPubKey.apply)
  implicit val implcitGetTxOutResult = jsonFormat6(GetTxOutResult.apply)


  // format: OFF
  val routes = {
    pathPrefix("") {

      (post & entity(as[RpcRequest])) { request =>
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

  val port = Config.scalechain.getInt("scalechain.api.port")
  val bindingFuture = Http().bindAndHandle(routes, "localhost", port)

  println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
  Console.readLine() // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.shutdown()) // and shutdown when done
}

