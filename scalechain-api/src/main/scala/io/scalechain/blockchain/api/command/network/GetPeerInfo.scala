package io.scalechain.blockchain.api.command.network


import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

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

case class PeerInfo(
  // (Since : 0.10.0) The node’s index number in the local node address database.
  id : Int, // 9
  // The IP address and port number used for the connection to the remote node.
  addr : String, // "192.0.2.113:18333"
  // Our IP address and port number according to the remote node. M
  // May be incorrect due to error or lying. Many SPV nodes set this to 127.0.0.1:8333
  addrlocal : Option[String], // "192.0.2.51:18333"
  // The services advertised by the remote node in its version message
  services : String, // "0000000000000002"
  // The Unix epoch time when we last successfully sent data to the TCP socket for this node
  lastsend : Long, // 1419277992
  // The Unix epoch time when we last received data from this node
  lastrecv : Long, // 1419277992
  // The total number of bytes we’ve sent to this node
  bytessent : Long, // 4968
  // The total number of bytes we’ve received from this node
  bytesrecv : Long,  // 105078
  // The Unix epoch time when we connected to this node
  conntime : Long, // 1419265985
  // The number of seconds this node took to respond to our last P2P ping message
  pingtime : scala.math.BigDecimal, // 0.05617800
  // The number of seconds we’ve been waiting for this node to respond to a P2P ping message.
  // Only shown if there’s an outstanding ping message
  pingwait : Option[scala.math.BigDecimal], // 0.04847123
  // The protocol version number used by this node. See the protocol versions section for more information
  version : Int, // 70001
  // The user agent this node sends in its version message.
  // This string will have been sanitized to prevent corrupting the JSON results. May be an empty string
  subver : String, // "/Satoshi:0.8.6/"
  // Set to true if this node connected to us; set to false if we connected to this node
  inbound : Boolean, // false
  // The height of the remote node’s block chain when it connected to us as reported in its version message
  startingheight : Long, // 315280
  // The ban score we’ve assigned the node based on any misbehavior it’s made.
  // By default, Bitcoin Core disconnects when the ban score reaches 100
  banscore : Int,  // 0
  // ( Since : 0.10.0 ) The highest-height header we have in common with this node based the last P2P headers message it sent us.
  // If a headers message has not been received, this will be set to -1
  synced_headers : Long,  // -1
  // ( Since : 0.10.0 ) The highest-height block we have in common with this node based on P2P inv messages this node sent us.
  // If no block inv messages have been received from this node, this will be set to -1
  synced_blocks : Long, // -1
  // ( Since : 0.10.0 ) An array of blocks which have been requested from this peer. May be empty
  // inflight item : The height of a block being requested from the remote peer.
  inflight : List[Long], // [],
  // ( Since : 0.10.0 )
  // Set to true if the remote peer has been whitelisted; otherwise, set to false.
  // Whitelisted peers will not be banned if their ban score exceeds the maximum (100 by default).
  // By default, peers connecting from localhost are whitelisted
  whitelisted : Boolean // false
)  

case class GetPeerInfoResult(peerInfos : List[PeerInfo]) extends RpcResult

/** GetPeerInfo: returns data about each connected network node.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getpeerinfo
  */
object GetPeerInfo extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {

    // TODO : Implement
    val peerInfos =
      List(
        PeerInfo(
          9,
          "192.0.2.113:18333",
          Some("192.0.2.51:18333"),
          "0000000000000002",
          1419277992,
          1419277992,
          4968,
          105078,
          1419265985,
          0.05617800,
          None,
          70001,
          "/Satoshi:0.8.6/",
          false,
          315280,
          0,
          -1,
          -1,
          List(),
          false
        )
      )

    Right( Some( GetPeerInfoResult( peerInfos ) ) )
  }
  def help() : String =
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
    """.stripMargin
}


