package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Get the balances for all non-empty accounts,
    # including only transactions which have been confirmed at least six times.
    bitcoin-cli -testnet listreceivedbyaccount 6 false

  CLI output :
    [
        {
            "account" : "",
            "amount" : 0.19960000,
            "confirmations" : 53601
        },
        {
            "account" : "doc test",
            "amount" : 0.30000000,
            "confirmations" : 8991
        }
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listreceivedbyaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ListReceivedByAccount: lists the total number of bitcoins received by each account.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listreceivedbyaccount
  */
object ListReceivedByAccount extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """listreceivedbyaccount ( minconf includeempty includeWatchonly)
      |
      |DEPRECATED. List balances by account.
      |
      |Arguments:
      |1. minconf      (numeric, optional, default=1) The minimum number of confirmations before payments are included.
      |2. includeempty (boolean, optional, default=false) Whether to include accounts that haven't received any payments.
      |3. includeWatchonly (bool, optional, default=false) Whether to include watchonly addresses (see 'importaddress').
      |
      |Result:
      |[
      |  {
      |    "involvesWatchonly" : true,   (bool) Only returned if imported addresses were involved in transaction
      |    "account" : "accountname",  (string) The account name of the receiving account
      |    "amount" : x.xxx,             (numeric) The total amount received by addresses with this account
      |    "confirmations" : n,          (numeric) The number of confirmations of the most recent transaction included
      |    "label" : "label"           (string) A comment for the address/transaction, if any
      |  }
      |  ,...
      |]
      |
      |Examples:
      |> bitcoin-cli listreceivedbyaccount
      |> bitcoin-cli listreceivedbyaccount 6 true
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "listreceivedbyaccount", "params": [6, true, true] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


