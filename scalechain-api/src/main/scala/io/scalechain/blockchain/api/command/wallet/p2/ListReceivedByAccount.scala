package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

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
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


