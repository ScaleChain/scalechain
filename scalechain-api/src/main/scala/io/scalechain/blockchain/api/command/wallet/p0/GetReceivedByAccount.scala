package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

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
object GetReceivedByAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


