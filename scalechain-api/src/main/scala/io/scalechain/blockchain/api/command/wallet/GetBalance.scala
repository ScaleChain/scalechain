package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getbalance "test1" 1 true

  CLI output :
    1.99900000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getbalance", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetBalance: gets the balance in decimal bitcoins across all accounts or for a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getbalance
  */
object GetBalance extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


