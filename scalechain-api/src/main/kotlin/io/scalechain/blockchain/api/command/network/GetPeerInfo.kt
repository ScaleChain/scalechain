package io.scalechain.blockchain.api.command.network


import io.scalechain.blockchain.api.RpcSubSystem
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.mining.SubmitBlock
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.net.PeerInfo
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


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


data class GetPeerInfoResult(val peerInfos : List<PeerInfo>) : RpcResult

/** GetPeerInfo: returns data about each connected network node.
  *
  * Updated in 0.10.0
  *
  * Parameters: none
  *
  * Result: (Array)
  *   An array of objects each describing one connected node.
  *   If there are no connections, the array will be empty.
  *
  * https://bitcoin.org/en/developer-reference#getpeerinfo
  */
object GetPeerInfo : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    return handlingException {
      val peerInfos = RpcSubSystem.get().getPeerInfos()
      Right(GetPeerInfoResult(peerInfos))
    }
  }
  override fun help() : String =
    """getpeerinfo
      |
      |Returns data about each connected network node as a json array of objects.
      |
      |Result:
      |[
      |  {
      |    "id": n,                   (numeric) Peer index
      |    "addr":"host:port",      (string) The ip address and port of the peer
      |    "addrlocal":"ip:port",   (string) local address
      |    "services":"xxxxxxxxxxxxxxxx",   (string) The services offered
      |    "relaytxes":true|false,    (boolean) Whether peer has asked us to relay transactions to it
      |    "lastsend": ttt,           (numeric) The time in seconds since epoch (Jan 1 1970 GMT) of the last send
      |    "lastrecv": ttt,           (numeric) The time in seconds since epoch (Jan 1 1970 GMT) of the last receive
      |    "bytessent": n,            (numeric) The total bytes sent
      |    "bytesrecv": n,            (numeric) The total bytes received
      |    "conntime": ttt,           (numeric) The connection time in seconds since epoch (Jan 1 1970 GMT)
      |    "timeoffset": ttt,         (numeric) The time offset in seconds
      |    "pingtime": n,             (numeric) ping time
      |    "minping": n,              (numeric) minimum observed ping time
      |    "pingwait": n,             (numeric) ping wait
      |    "version": v,              (numeric) The peer version, such as 7001
      |    "subver": "/Satoshi:0.8.5/",  (string) The string version
      |    "inbound": true|false,     (boolean) Inbound (true) or Outbound (false)
      |    "startingheight": n,       (numeric) The starting height (block) of the peer
      |    "banscore": n,             (numeric) The ban score
      |    "synced_headers": n,       (numeric) The last header we have in common with this peer
      |    "synced_blocks": n,        (numeric) The last block we have in common with this peer
      |    "inflight": [
      |       n,                        (numeric) The heights of blocks we're currently asking from this peer
      |       ...
      |    ]
      |    "bytessent_per_msg": {
      |       "addr": n,             (numeric) The total bytes sent aggregated by message type
      |       ...
      |    }
      |    "bytesrecv_per_msg": {
      |       "addr": n,             (numeric) The total bytes received aggregated by message type
      |       ...
      |    }
      |  }
      |  ,...
      |]
      |
      |Examples:
      |> bitcoin-cli getpeerinfo
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getpeerinfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


