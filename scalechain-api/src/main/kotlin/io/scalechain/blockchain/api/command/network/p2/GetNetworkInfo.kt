package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    bitcoin-cli -testnet getnetworkinfo

  CLI output :
    {
        "version" : 100000,
        "subversion" : "/Satoshi:0.10.0/",
        "protocolversion" : 70002,
        "localservices" : "0000000000000001",
        "timeoffset" : 0,
        "connections" : 51,
        "networks" : [
            {
                "name" : "ipv4",
                "limited" : false,
                "reachable" : true,
                "proxy" : ""
            },
            {
                "name" : "ipv6",
                "limited" : false,
                "reachable" : true,
                "proxy" : ""
            },
            {
                "name" : "onion",
                "limited" : false,
                "reachable" : false,
                "proxy" : ""
            }
        ],
        "relayfee" : 0.00001000,
        "localaddresses" : [
            {
                "address" : "192.0.2.113",
                "port" : 18333,
                "score" : 6470
            },
            {
                "address" : "0600:3c03::f03c:91ff:fe89:dfc4",
                "port" : 18333,
                "score" : 2029
            }
        ]
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnetworkinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNetworkInfo: returns information about the node’s connection to the network.
  *
  * Since - New in 0.9.2, Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getnetworkinfo
  */
object GetNetworkInfo : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
"""

"""
}


