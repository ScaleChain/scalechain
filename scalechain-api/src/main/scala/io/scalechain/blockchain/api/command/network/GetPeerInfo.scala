package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getpeerinfo

  CLI output :
    [
        {
            "id" : 9,
            "addr" : "192.0.2.113:18333",
            "addrlocal" : "192.0.2.51:18333",
            "services" : "0000000000000002",
            "lastsend" : 1419277992,
            "lastrecv" : 1419277992,
            "bytessent" : 4968,
            "bytesrecv" : 105078,
            "conntime" : 1419265985,
            "pingtime" : 0.05617800,
            "version" : 70001,
            "subver" : "/Satoshi:0.8.6/",
            "inbound" : false,
            "startingheight" : 315280,
            "banscore" : 0,
            "synced_headers" : -1,
            "synced_blocks" : -1,
            "inflight" : [
            ],
            "whitelisted" : false
        }
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getpeerinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetPeerInfo: returns data about each connected network node.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getpeerinfo
  */
object GetPeerInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


