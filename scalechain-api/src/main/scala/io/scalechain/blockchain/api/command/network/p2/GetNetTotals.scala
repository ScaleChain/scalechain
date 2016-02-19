package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getnettotals

  CLI output :
    {
        "totalbytesrecv" : 723992206,
        "totalbytessent" : 16846662695,
        "timemillis" : 1419268217354
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnettotals", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNetTotals: returns information about network traffic,
  * including bytes in, bytes out, and the current time.
  *
  * https://bitcoin.org/en/developer-reference#getnettotals
  */
object GetNetTotals extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


