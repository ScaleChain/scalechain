package io.scalechain.blockchain.api

import com.typesafe.config.ConfigFactory
import io.scalechain.blockchain.api.command.blockchain.p1.{GetTxOutResult, ScriptPubKey}
import io.scalechain.blockchain.api.command.control.p1.GetInfoResult
import io.scalechain.blockchain.api.command.blockchain._
import io.scalechain.blockchain.api.command.wallet._
import io.scalechain.blockchain.api.command.network._
import io.scalechain.blockchain.api.command.rawtx._

import io.scalechain.blockchain.api.domain._
import io.scalechain.blockchain.api.http.ApiServer
import io.scalechain.blockchain.net.PeerInfo
import io.scalechain.blockchain.proto.{HashFormat}
import io.scalechain.util.StringUtil
import io.scalechain.wallet.UnspentCoinDescriptor
import io.scalechain.wallet.WalletTransactionDescriptor
import org.apache.http.protocol.ExecutionContext
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.ExecutionContextExecutor

//import scala.io.StdIn

// TODO : Need to move to scalechain-api-domain?
object RpcResultJsonFormat {
  import HashFormat._

  implicit val implicitGetInfoResult = jsonFormat15(GetInfoResult.apply)

  implicit val implicitScriptPubKey = jsonFormat5(ScriptPubKey.apply)
  implicit val implicitGetTxOutResult = jsonFormat6(GetTxOutResult.apply)

  implicit val implicitGetBlockResult = jsonFormat9(GetBlockResult.apply)

  implicit val implicitPeerInfoResult = jsonFormat5(PeerInfo.apply)


  import RawTransactionInputJsonFormat._

  implicit val implicitRawScriptPubKey               = jsonFormat1(RawScriptPubKey.apply)
  implicit val implicitRawTransactionOutput          = jsonFormat3(RawTransactionOutput.apply)
  implicit val implicitDecodedRawTransaction         = jsonFormat5(DecodedRawTransaction.apply)
  implicit val implicitRawTransaction                = jsonFormat10(RawTransaction.apply)

  implicit val implicitSignRawTransactionResult      = jsonFormat2(SignRawTransactionResult.apply)

  implicit val implicitTransactionDescriptor         = jsonFormat14(WalletTransactionDescriptor.apply)

  implicit val implicitUnspentCoin                   = jsonFormat9(UnspentCoinDescriptor.apply)


  implicit object format extends RootJsonFormat[RpcResult] {
    def write(result : RpcResult) = result match {

      case StringResult(value)            => JsString(value)
      case StringListResult(values)       => JsArray(values.map(JsString(_)).toVector)
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
        "result" -> rpcResponse.result.map( _.toJson ).getOrElse(JsNull),
        "error"  -> rpcResponse.error.map( _.toJson ).getOrElse(JsNull),
        "id" -> JsNumber(rpcResponse.id)
      )

    // Not used.
    def read(value: JsValue) = {
      assert(false)
      null
    }
  }
}

object JsonRpcMicroservice extends JsonRpcMicroservice

class JsonRpcMicroservice {
  def runService(inboundPort : Int) = {
    new ApiServer().listen(inboundPort)
  }
}

