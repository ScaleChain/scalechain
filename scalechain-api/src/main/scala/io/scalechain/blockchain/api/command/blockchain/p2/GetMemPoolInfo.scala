package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getmempoolinfo

  CLI output :
    {
      "size" : 37,
      "bytes" : 9423
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getmempoolinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetMemPoolInfo: returns information about the node’s current transaction memory pool.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getmempoolinfo
  */
object GetMemPoolInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


