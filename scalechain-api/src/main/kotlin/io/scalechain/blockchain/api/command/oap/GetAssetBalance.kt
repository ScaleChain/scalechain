package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain._
import io.scalechain.blockchain.oap.{AssetBalanceDesc, OpenAssetsProtocol}
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConverters


/** GetBalance: gets the balance in decimal bitcoins across all accounts or for a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getbalance
  *
  * Parameter #1 Account (String, required) Account
  * Parameter #2 minimum confirmations
  * Parameter #3 include watch-only
  * Parameter #4 asset id filter (String, optional) Array of asset ids in base58 check format
  *   include only assets in this list.
  *
  * Json-RPC request :
  * {"jsonrpc": "1.0", "id":"curltest", "method": "getassetbalance", "params": [ "SENDER", 1, true, [] ] }
  *
  *Json-RPC response :
  * {
  *   "result": {
  *     "oWW5DyHMmNpH2P9gGwDH7kLw7mgt1iV4W8" : 10000
  *   },
  *   "error": null,
  *   "id": "curltest"
  * }
  *
  */
object GetAssetBalance extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      val account:          String       = request.params.getOption[String] ("Account", 0).getOrElse("")
      val minconf:          Long         = request.params.getOption[Long]("minimum confirmations", 1).getOrElse(1)
      val includeWatchOnly: Boolean      = request.params.getOption[Boolean]("include watch-only", 2).getOrElse(false)
      val assetIds:         List[String] = request.params.getOption[List[String]]("assetIdFilter", 3).getOrElse(List.empty[String]);

      val accountOption = if (account == "*") None else Some(account)

      import scala.collection.JavaConverters._
      val balances : List[AssetBalanceDesc] = OpenAssetsProtocol.get().getAssetBalance(
        accountOption,
        minconf,
        includeWatchOnly,
        assetIds.asJava
      ).asScala.toList

      Right(Some(
        ListAssetBalanceDescResult(balances)
      ))
    }
  }

  def help() : String =
    """getbalance ( "account" minconf includeWatchonly )
      |
      |If account is not specified, returns the server's total available asset balance.
      |If account is specified (DEPRECATED), returns the asset balance in the account.
      |Note that the account "" is not the same as leaving the parameter out.
      |The server total may be different to the balance in the default "" account.
      |
      |Arguments:
      |1. "account"      (string, optional) The selected account, or "*" for entire wallet. It may be the default account using "".
      |2. minconf          (numeric, optional, default=1) Only include transactions confirmed at least this many times.
      |3. includeWatchonly (bool, optional, default=false) Also include balance in watchonly addresses (see 'importaddress')
      |4. asset id filter (string, optional, default=[]) include balacne of assets in this list
      |
      |Result:
      |Asset Balacne List
      |  asset_id (String) the asset id
      |  quantity (numeric) total quantity of the asset
      |
      |Examples:
      |
      |The total amount in the wallet
      |> bitcoin-cli getbalance
      |
      |The total amount in the wallet at least 5 blocks confirmed
      |> bitcoin-cli getbalance "*" 6
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getassetbalance", "params": ["SENDER", 1, true, []] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}

case class ListAssetBalanceDescResult( transactionDescs : List[AssetBalanceDesc] ) extends RpcResult
