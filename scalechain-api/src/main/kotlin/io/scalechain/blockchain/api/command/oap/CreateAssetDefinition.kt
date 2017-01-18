package io.scalechain.blockchain.api.command.oap

import io.scalechain.blockchain.{ErrorCode, GeneralException, ScriptParseException}
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain._
import io.scalechain.blockchain.oap.{AssetDefinitionHandler, OpenAssetsProtocol}
import io.scalechain.blockchain.oap.exception.OapException
import io.scalechain.blockchain.oap.wallet.AssetId
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.transaction.{CoinAddress, ParsedPubKeyScript}
import io.scalechain.util.{ByteArray, HexUtil}
import spray.json.JsObject
import spray.json.DefaultJsonProtocol._

/**
  * Creates Asset Definition and returns Asset Definition and Asset Definition Pointer
  *
  * Parameter #1 assetIdOrAddress  String(required) Asset Id or Asset Address in base58 check format.
  *
  * Parameter #2 metadata Object(require) Contents of Asset Definition File in JsonObject.
  *     metadata should not cointain asset_ids fields. CreateAssetDefinition will add asset_id field.
  *     metadata should contain mandatory filed name and name_short field or Eorror will be thrown.
  *
  * Json-RPC request :
  * {"jsonrpc": "1.0", "id":"curltest", "method": "getassetaddress", "params": ["oWW5DyHMmNpH2P9gGwDH7kLw7mgt1iV4W8", {"name":"OAP Test Asset 7","name_short":"TestAsset7"}] }
  *
  * Json-RPC response :
  *  {
  *   "result": {
  *       "address" : "n3xRLJSdJrWngv9yUYZDeepMbqU5decwbG",
  *        "asset_adress" : "bXDvJaUFxnLQUZAKLXAfQJWivwSeFcg25Wz",
  *        "asset_id" : "oWW5DyHMmNpH2P9gGwDH7kLw7mgt1iV4W8"
  *    },
  *    "error": null,
  *    "id": "curltest"
  * }
  *
  *
  *
  * Created by shannon on 16. 12. 28.
  */
object CreateAssetDefinition extends RpcCommand {

  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      val assetIdOrAddress: String           = request.params.get[String]("assetIdOrAddress", 0)
      val metadata:         JsObject         = request.params.get[JsObject]("metadata", 1)

      val assetId =
        try {
          AssetId.from(CoinAddress.from(assetIdOrAddress))
        } catch {
          case e : GeneralException => {
            try {
              AssetId.from(assetIdOrAddress)
            } catch {
              case e : OapException => {
                throw new GeneralException(ErrorCode.RpcInvalidAddress)
              }
            }
          }
        }

      val definition = AssetDefinitionHandler.get().createAssetDefinition(assetId, metadata.toString())

      import spray.json._
      Right(Some(JsResult(
        JsObject(
          ("asset_id",         JsString(assetId.base58())),
          ("metadata_hash",             JsString(HexUtil.hex(definition.getFirst.getValue))),
          ("asset_definition", definition.getSecond.toString.toJson)
        )
      )))
    }
  }

  def help() : String =
    """createassetdefinition "assIdOrAddress" "metadata"
      |
      |create an asset definition file.
      |
      |Asset Definitiona File is created and it's hash value is calculated.
      |
      |Arguments:
      |1. "assetIdOrAddress"   (string, required) Asset ID or Issuing Address
      |3. "metadata"           (Object, required) JSON Object containing metadata.
      |
      |Result:
      |{
      |  "result" : {
      |   "asset_id" : "",
      |   "hash" : "",
      |   "asset_defintion" : {
      |     "asset_ids" : [ ],
      |     "name" : "",
      |     "name_short" : ""
      |   }
      |  }
      |}
      |Examples:
      |> bitcoin-cli issueasset "issuer_address" "to_address" "quantity" "metadata" "change_address" "fees"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getassetaddress", "params": ["oWW5DyHMmNpH2P9gGwDH7kLw7mgt1iV4W8", {"name":"OAP Test Asset 7","name_short":"TestAsset7"}] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}