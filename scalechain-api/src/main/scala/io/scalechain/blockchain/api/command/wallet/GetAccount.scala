package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getaccount mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN

  CLI output :
    doc test

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAccount: returns the name of the account associated with the given address.
  *
  * https://bitcoin.org/en/developer-reference#getaccount
  */
object GetAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


