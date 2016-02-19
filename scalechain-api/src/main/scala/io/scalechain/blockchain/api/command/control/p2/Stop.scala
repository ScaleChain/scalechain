package io.scalechain.blockchain.api.command.control.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet stop

  CLI output :
    Bitcoin server stopping

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "stop", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** Stop: safely shuts down the Bitcoin Core server.
  *
  * https://bitcoin.org/en/developer-reference#stop
  */
object Stop extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


