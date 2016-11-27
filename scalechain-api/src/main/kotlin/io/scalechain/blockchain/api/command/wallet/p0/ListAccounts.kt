package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    # Display account balances with one confirmation and watch-only addresses included.
    bitcoin-cli -testnet listaccounts 1 true

  CLI output :
    {
        "" : -2.73928803,
        "Refund from example.com" : 0.00000000,
        "doc test" : -498.45900000,
        "someone else's address" : 0.00000000,
        "someone else's address2" : 0.00050000,
        "test" : 499.97975293,
        "test account" : 0.00000000,
        "test label" : 0.48961280,
        "test1" : 1.99900000
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listaccounts", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ListAccounts: lists accounts and their balances.
  * - Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listaccounts
  */
object ListAccounts : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """listaccounts ( minconf includeWatchonly)
      |
      |DEPRECATED. Returns Object that has account names as keys, account balances as values.
      |
      |Arguments:
      |1. minconf          (numeric, optional, default=1) Only include transactions with at least this many confirmations
      |2. includeWatchonly (bool, optional, default=false) Include balances in watchonly addresses (see 'importaddress')
      |
      |Result:
      |{                      (json object where keys are account names, and values are numeric balances
      |  "account": x.xxx,  (numeric) The property name is the account name, and the value is the total balance for the account.
      |  ...
      |}
      |
      |Examples:
      |
      |List account balances where there at least 1 confirmation
      |> bitcoin-cli listaccounts
      |
      |List account balances including zero confirmation transactions
      |> bitcoin-cli listaccounts 0
      |
      |List account balances for 6 or more confirmations
      |> bitcoin-cli listaccounts 6
      |
      |As json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "listaccounts", "params": [6] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


