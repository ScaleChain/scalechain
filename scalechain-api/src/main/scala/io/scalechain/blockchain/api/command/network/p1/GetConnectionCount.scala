package io.scalechain.blockchain.api.command.network.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getconnectioncount

  CLI output :
    14

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getconnectioncount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetConnectionCount: returns the number of connections to other nodes.
  *
  * https://bitcoin.org/en/developer-reference#getconnectioncount
  */
object GetConnectionCount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


