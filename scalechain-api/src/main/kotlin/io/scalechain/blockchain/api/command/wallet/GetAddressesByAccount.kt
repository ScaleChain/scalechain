package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult, StringListResult}
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.oap.OpenAssetsProtocol
import io.scalechain.blockchain.transaction.{CoinAddress, OutputOwnership}
import io.scalechain.wallet.Wallet
import spray.json.DefaultJsonProtocol._

/*
  CLI command :
    bitcoin-cli -testnet getaddressesbyaccount "doc test"

  CLI output :
    [
        "mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN",
        "mft61jjkmiEJwJ7Zw3r1h344D6aL1xwhma",
        "mmXgiR6KAhZCyQ8ndr2BCfEq1wNG2UnyG6"
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaddressbyaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAddressesByAccount: returns a list of every address assigned to a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getaddressesbyaccount
  */
object GetAddressesByAccount extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      val account: String           = request.params.getOption[String] ("Account", 0).getOrElse("")

      val accountOption = if (account == "*") None else Some(account)
      import scala.collection.JavaConverters;
      val addresses : List[String] = JavaConverters.asScalaBuffer(
        OpenAssetsProtocol.get().getAddressesByAccount(accountOption, true)
      ).map(a => a.base58()).toList;

      Right(Some(StringListResult(addresses)))
    }
  }
  def help() : String =
    """getaddressesbyaccount "account"
      |
      |DEPRECATED. Returns the list of addresses for the given account.
      |
      |Arguments:
      |1. "account"  (string, required) The account name.
      |
      |Result:
      |[                     (json array of string)
      |  "bitcoinaddress"  (string) a bitcoin address associated with the given account
      |  ,...
      |]
      |
      |Examples:
      |> bitcoin-cli getaddressesbyaccount "tabby"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getaddressesbyaccount", "params": ["tabby"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


