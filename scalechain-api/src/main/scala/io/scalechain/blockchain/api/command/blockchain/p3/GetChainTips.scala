package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getchaintips

  CLI output :
    [
        {
            "height" : 312647,
            "hash" : "000000000b1be96f87b31485f62c1361193304a5ad78acf47f9164ea4773a843",
            "branchlen" : 0,
            "status" : "active"
        },
        {
            "height" : 282072,
            "hash" : "00000000712340a499b185080f94b28c365d8adb9fc95bca541ea5e708f31028",
            "branchlen" : 5,
            "status" : "valid-fork"
        },
        {
            "height" : 281721,
            "hash" : "000000006e1f2a32199629c6c1fbd37766f5ce7e8c42bab0c6e1ae42b88ffe12",
            "branchlen" : 1,
            "status" : "valid-headers"
        }
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getchaintips", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetChainTips: returns information about the highest-height block (tip) of each local block chain.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getchaintips
  */
object GetChainTips extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


