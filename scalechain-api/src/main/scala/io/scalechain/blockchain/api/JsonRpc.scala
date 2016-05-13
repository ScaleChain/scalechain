package io.scalechain.blockchain.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import io.scalechain.blockchain.api.command.blockchain.p1.{GetTxOutResult, ScriptPubKey}
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.control.p1.GetInfoResult
import io.scalechain.blockchain.api.command.blockchain._
import io.scalechain.blockchain.api.command.wallet._
import io.scalechain.blockchain.api.command.control._
import io.scalechain.blockchain.api.command.generating._
import io.scalechain.blockchain.api.command.help._
import io.scalechain.blockchain.api.command.mining._
import io.scalechain.blockchain.api.command.network._
import io.scalechain.blockchain.api.command.rawtx._
import io.scalechain.blockchain.api.command.utility._

import io.scalechain.blockchain.api.domain._
import io.scalechain.blockchain.net.service.PeerInfo
import io.scalechain.blockchain.proto.{HashFormat, Hash}
import io.scalechain.wallet.UnspentCoin
import io.scalechain.wallet.Wallet.TransactionDescriptor
import spray.json.DefaultJsonProtocol._
import spray.json._
import io.scalechain.util.{ByteArray, HexUtil, Config}

// TODO : Need to move to scalechain-api-domain?
object RpcResultJsonFormat {
  import HashFormat._

  implicit val implicitGetInfoResult = jsonFormat15(GetInfoResult.apply)

  implicit val implicitScriptPubKey = jsonFormat5(ScriptPubKey.apply)
  implicit val implicitGetTxOutResult = jsonFormat6(GetTxOutResult.apply)

  implicit val implicitGetBlockResult = jsonFormat9(GetBlockResult.apply)

  implicit val implicitPeerInfoResult = jsonFormat4(PeerInfo.apply)


  import RawTransactionInputJsonFormat._

  implicit val implicitRawScriptPubKey               = jsonFormat1(RawScriptPubKey.apply)
  implicit val implicitRawTransactionOutput          = jsonFormat3(RawTransactionOutput.apply)
  implicit val implicitDecodedRawTransaction         = jsonFormat5(DecodedRawTransaction.apply)
  implicit val implicitRawTransaction                = jsonFormat6(RawTransaction.apply)

  implicit val implicitSignRawTransactionResult      = jsonFormat2(SignRawTransactionResult.apply)

  implicit val implicitTransactionDescriptor         = jsonFormat19(TransactionDescriptor.apply)

  implicit val implicitUnspentCoin                   = jsonFormat9(UnspentCoin.apply)


  implicit object format extends RootJsonFormat[RpcResult] {
    def write(result : RpcResult) = result match {

      case StringResult(value)            => JsString(value)
      case NumberResult(value)            => JsNumber(value)

      case GetPeerInfoResult( items )     => items.toJson
      case ListTransactionsResult(items)  => items.toJson
      case ListUnspentResult(items)       => items.toJson

      case r : DecodedRawTransaction      => r.toJson
      case r : RawTransaction             => r.toJson

      case r : GetInfoResult              => r.toJson
      case r : GetTxOutResult             => r.toJson

      case r : GetBlockResult             => r.toJson
      case r : SignRawTransactionResult   => r.toJson

      // RPCs that returns String do not require to map the result to Json
      // case r : GetBestBlockHashResult     => r.toJson
      // case r : GetBlockHashResult         => r.toJson
      // case r : GetAccountResult           => r.toJson
      // case r : GetNewAddressResult        => r.toJson
      // case r : GetAccountAddressResult    => r.toJson
      // case r : GetReceivedByAddressResult => r.toJson
      // case r : SubmitBlockResult          => r.toJson
      // case r : SendRawTransactionResult   => r.toJson
      // case r : SendFromResult             => r.toJson
      // case r : HelpResult                 => r.toJson
    }

    // Not used.
    def read(value:JsValue) = {
      assert(false)
      null
    }
  }
}

/** Serializes RpcResponse to Json string.
  *
  * Reason of not using jsonFormat3 :
  *   We need to serialize RpcResponse.result to null when it is None.
  *   ( Need to do the same serialization for the RpcResponse.error field. )
  */
object RpcResponseJsonFormat {
  import RpcResultJsonFormat._

  implicit val implicitJsonRpcError = jsonFormat3(RpcError.apply)

  implicit object formatter extends RootJsonFormat[RpcResponse] {
    def write(rpcResponse: RpcResponse) =
      JsObject(
        //"result" -> rpcResponse.result.map( JsonRpcResultFormat.write(_) ).getOrElse(JsNull),
        "result" -> rpcResponse.result.map( _.toJson ).getOrElse(JsNull),
        //"error"  -> rpcResponse.error.map( implicitJsonRpcError.write(_) ).getOrElse(JsNull),
        "error"  -> rpcResponse.error.map( _.toJson ).getOrElse(JsNull),
        "id" -> JsString(rpcResponse.id)
      )

    // Not used.
    def read(value: JsValue) = {
      assert(false)
      null
    }
  }
}


// use it wherever json (un)marshalling is needed
trait JsonRpc extends Directives with SprayJsonSupport with DefaultJsonProtocol with ServiceDispatcher {
/*
  implicit object ScalaValueFormat extends RootJsonFormat[AnyRef] {
    def write(value : AnyRef) = value.toJson
    def read(value :JsValue) = ""
  }
*/

  import RpcResultJsonFormat._



  /*
  implicit object AnyJsonFormat extends RootJsonFormat[Any] {
    def write( any : Any ) = {
      // Not used.
      assert(false);
      "".toJson
    }

    def read( value: JsValue ) : Any = {
      value match {
        case v : JsString => v.convertTo[String]
        case v : JsNumber => v.convertTo[scala.math.BigDecimal]
        case v : JsBoolean => v.convertTo[Boolean]
        case _ => assert(false)
      }
    }
  }
  */

  import RpcParamsJsonFormat._

  implicit val implicitJsonRpcRequest = jsonFormat4(RpcRequest.apply)

  import RpcResponseJsonFormat._

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
  runService()

  def runService() = {
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
}

