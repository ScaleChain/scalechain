package io.scalechain.blockchain.api.command.mining.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getnetworkhashps -1 227255

  CLI output :
    79510076167

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnetworkhashps", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNetworkHashPS: returns the estimated current or historical network hashes per second based on the last n blocks.
  *
  * https://bitcoin.org/en/developer-reference#getnetworkhashps
  */
object GetNetworkHashPS extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


