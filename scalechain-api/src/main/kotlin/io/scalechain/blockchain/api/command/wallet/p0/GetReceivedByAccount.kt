package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    # Get the coins received by the “doc test” account with six or more confirmations.
    bitcoin-cli -testnet getreceivedbyaccount "doc test" 6

  CLI output :
    0.30000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getreceivedbyaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetReceivedByAccount: returns the total amount received by addresses in a particular account from
  * transactions with the specified number of confirmations. It does not count coinbase transactions.
  *
  * https://bitcoin.org/en/developer-reference#getreceivedbyaccount
  */
object GetReceivedByAccount : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """getreceivedbyaccount "account" ( minconf )
      |
      |DEPRECATED. Returns the total amount received by addresses with <account> in transactions with at least [minconf] confirmations.
      |
      |Arguments:
      |1. "account"      (string, required) The selected account, may be the default account using "".
      |2. minconf          (numeric, optional, default=1) Only include transactions confirmed at least this many times.
      |
      |Result:
      |amount              (numeric) The total amount in BTC received for this account.
      |
      |Examples:
      |
      |Amount received by the default account with at least 1 confirmation
      |> bitcoin-cli getreceivedbyaccount ""
      |
      |Amount received at the tabby account including unconfirmed amounts with zero confirmations
      |> bitcoin-cli getreceivedbyaccount "tabby" 0
      |
      |The amount with at least 6 confirmation, very safe
      |> bitcoin-cli getreceivedbyaccount "tabby" 6
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getreceivedbyaccount", "params": ["tabby", 6] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


