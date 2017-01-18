package io.scalechain.blockchain.api.command.oap

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain._
import io.scalechain.blockchain.net.IssueAssetResult
import io.scalechain.blockchain.oap.exception.OapException
import io.scalechain.blockchain.oap.wallet.{AssetAddress, AssetId}
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.util.HexUtil
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsString}

/*
*/

/** GetAssetAddress:
  *  gets AssetAddress and AssetId for given Address.
  *
  * https://bitcoin.org/en/developer-reference#getbalance
  *
  * Json-RPC request :
  * {"jsonrpc": "1.0", "id":"curltest", "method": "getassetaddress", "params": ["n3xRLJSdJrWngv9yUYZDeepMbqU5decwbG"] }
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
  */
object GetAssetAddress extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      val address: String           = request.params.get[String]("address", 0)

      val coinAddress : CoinAddress =
        try {
          CoinAddress.from(address);
        } catch {
          case ge : GeneralException => {
            try {
              AssetAddress.from(address).coinAddress();
            } catch {
              case oe : OapException => {
                throw new GeneralException(ErrorCode.RpcInvalidAddress)
              }
            }
          }
        }

      Right(Some(JsResult(
        JsObject(
          ("address",       JsString(address)),
          ("asset_address", JsString(AssetAddress.fromCoinAddress(coinAddress).base58())),
          ("asset_id",      JsString(AssetId.from(coinAddress).base58()))
        )
      )))
    }
  }

  def help() : String =
    """getAssetAddress ( "account" )
      |
      |Arguments:
      |1. "address"      (string, require) The bitcoin address in base58 check format.
      |
      |Result:
      |address           (String) The bitcoin address
      |asset_address     (String) The Asset address of the bitcoin address
      |asset_id          (String) The Asset Id of the bitcoin address
      |
      |Examples:
      |
      |The total amount in the wallet
      |
      |As a json rpc call
      |
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getassetaddress", "params": ["n3xRLJSdJrWngv9yUYZDeepMbqU5decwbG"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}