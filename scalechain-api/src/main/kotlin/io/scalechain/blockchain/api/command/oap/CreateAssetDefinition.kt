package io.scalechain.blockchain.api.command.oap

import com.google.gson.JsonObject
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.*
import io.scalechain.blockchain.oap.AssetDefinitionHandler
import io.scalechain.blockchain.oap.exception.OapException
import io.scalechain.blockchain.oap.wallet.AssetId
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.util.HexUtil
import io.scalechain.util.Either
import io.scalechain.util.Either.Right
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

data class CreateAssetDefinitionResult(val asset_id: String, val metadata_hash : String, val asset_definition : JsonObject) : RpcResult

object CreateAssetDefinition : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val assetIdOrAddress: String           = request.params.get<String>("assetIdOrAddress", 0)
      // BUGBUG : Make sure this works
      val metadata: JsonObject               = request.params.get<JsonObject>("metadata", 1)

      val assetId =
        try {
          AssetId.from(CoinAddress.from(assetIdOrAddress))
        } catch(e : GeneralException) {
          try {
            AssetId.from(assetIdOrAddress)
          } catch(e : OapException) {
            throw GeneralException(ErrorCode.RpcInvalidAddress)
          }
        }

      val definition = AssetDefinitionHandler.get().createAssetDefinition(assetId, metadata.toString())

      // BUGBUG : OAP : Make sure the following field is serialized correctly : asset_definition : JsonObject
      Right(
        CreateAssetDefinitionResult(
          assetId.base58(),
          HexUtil.hex(definition.first.value),
          definition.second.toJson()
        )
      )
    }
  }

  override fun help() : String =
    """createassetdefinition "assIdOrAddress" "metadata"
      |
      |create an asset definition file.
      |
      |Asset Definitiona File is created and it's hash value is calculated.
      |
      |Arguments:
      |1. "assetIdOrAddress"   (string, required) Asset ID or Issuing Address
      |2. "metadata"           (Object, required) JSON Object containing metadata.
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
    """.trimMargin()
}