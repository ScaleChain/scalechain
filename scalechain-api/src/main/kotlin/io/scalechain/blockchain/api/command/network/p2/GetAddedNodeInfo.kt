package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getaddednodeinfo true

  CLI output :
    [
        {
            "addednode" : "bitcoind.example.com:18333",
            "connected" : true,
            "addresses" : [
                {
                    "address" : "192.0.2.113:18333",
                    "connected" : "outbound"
                }
            ]
        }
    ]
  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaddednodeinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetAddedNodeInfo: returns information about the given added node, or
  * all added nodes (except onetry nodes).
  *
  * Only nodes which have been manually added using the addnode RPC will have their information displayed.
  *
  * https://bitcoin.org/en/developer-reference#getaddednodeinfo
  */
object GetAddedNodeInfo : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
"""

"""
}


