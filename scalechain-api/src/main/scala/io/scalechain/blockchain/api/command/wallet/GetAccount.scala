package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.{ErrorCode, UnsupportedFeature}
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.rawtx.GetRawTransaction._
import io.scalechain.blockchain.api.domain.{StringResult, RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.HashFormat
import io.scalechain.wallet.{CoinAddress, Wallet}
import io.scalechain.wallet.util.Base58Check
import spray.json.DefaultJsonProtocol._

/*
  CLI command :
    bitcoin-cli -testnet getaccount mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN

  CLI output :
    doc test

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": ["mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN"] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAccount: returns the name of the account associated with the given address.
  *
  * Parameter #1 : Address (String;base58, Required)
  *   A P2PKH or P2SH address belonging either to a specific account or the default account (“”).
  *
  * Result: (String)
  *   The name of an account, or an empty string (“”, the default account).
  *
  * https://bitcoin.org/en/developer-reference#getaccount
  */
object GetAccount extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    handlingException {
      val address: String = request.params.get[String]("Address", 0)

      val coinAddress = CoinAddress.from(address)

      val accountName = Wallet.getAccount(coinAddress)
      Right(Some(StringResult(accountName)))
    }
  }
  def help() : String =
    """getaccount "bitcoinaddress"
      |
      |DEPRECATED. Returns the account associated with the given address.
      |
      |Arguments:
      |1. "bitcoinaddress"  (string, required) The bitcoin address for account lookup.
      |
      |Result:
      |"accountname"        (string) the account address
      |
      |Examples:
      |> bitcoin-cli getaccount "1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": ["1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


