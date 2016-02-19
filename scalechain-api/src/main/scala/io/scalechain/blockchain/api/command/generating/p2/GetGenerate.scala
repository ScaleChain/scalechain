package io.scalechain.blockchain.api.command.generating.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getgenerate

  CLI output :
    false

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getgenerate", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetGenerate: returns true if the node is set to generate blocks using its CPU.
  *
  * https://bitcoin.org/en/developer-reference#getgenerate
  */
object GetGenerate extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


