package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

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
object ListAccounts extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


