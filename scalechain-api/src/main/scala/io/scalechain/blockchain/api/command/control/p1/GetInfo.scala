package io.scalechain.blockchain.api.command.control.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getinfo

  CLI output :
    {
      "version": 110100,
      "protocolversion": 70002,
      "walletversion": 60000,
      "balance": 0,
      "blocks": 394722,
      "timeoffset": -24,
      "connections": 8,
      "proxy": "",
      "difficulty": 113354299801.47,
      "testnet": false,
      "keypoololdest": 1445528771,
      "keypoolsize": 101,
      "paytxfee": 0,
      "relayfee": 5.0e-5,
      "errors": ""
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getinfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class GetInfoResult(
  version : Int,
  protocolversion : Int,
  walletversion : Int,
  balance: Int,
  blocks: Int,
  timeoffset: Int,
  connections : Int,
  proxy: String,
  difficulty: scala.math.BigDecimal,
  testnet: Boolean,
  keypoololdest: Long,
  keypoolsize: Int,
  paytxfee : Int,
  // Make sure the Json serialized format is like "5.0e-5"
  relayfee: scala.math.BigDecimal,
  errors: String
) extends RpcResult

/** GetInfo: prints various information about the node and the network.
  *
  * Updated in 0.10.0, Deprecated
  *
  * https://bitcoin.org/en/developer-reference#getinfo
  */
object GetInfo extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    Right(
      Some(
        GetInfoResult(
          version = 110100,
          protocolversion = 70002,
          walletversion = 60000,
          balance = 0,
          blocks = 394722,
          timeoffset = -24,
          connections = 8,
          proxy = "",
          difficulty = new java.math.BigDecimal(113354299801.47),
          testnet = false,
          keypoololdest = 1445528771,
          keypoolsize = 101,
          paytxfee = 0,
          // Make sure the Json serialized format is like "5.0e-5"
          relayfee = new java.math.BigDecimal(5.0e-5),
          errors = ""
        )
      )
    )
  }
  def help() : String =
    """getinfo
      |Returns an object containing various state info.
      |
      |Result:
      |{
      |  "version": xxxxx,           (numeric) the server version
      |  "protocolversion": xxxxx,   (numeric) the protocol version
      |  "walletversion": xxxxx,     (numeric) the wallet version
      |  "balance": xxxxxxx,         (numeric) the total bitcoin balance of the wallet
      |  "blocks": xxxxxx,           (numeric) the current number of blocks processed in the server
      |  "timeoffset": xxxxx,        (numeric) the time offset
      |  "connections": xxxxx,       (numeric) the number of connections
      |  "proxy": "host:port",     (string, optional) the proxy used by the server
      |  "difficulty": xxxxxx,       (numeric) the current difficulty
      |  "testnet": true|false,      (boolean) if the server is using testnet or not
      |  "keypoololdest": xxxxxx,    (numeric) the timestamp (seconds since GMT epoch) of the oldest pre-generated key in the key pool
      |  "keypoolsize": xxxx,        (numeric) how many new keys are pre-generated
      |  "unlocked_until": ttt,      (numeric) the timestamp in seconds since epoch (midnight Jan 1 1970 GMT) that the wallet is unlocked for transfers, or 0 if the wallet is locked
      |  "paytxfee": x.xxxx,         (numeric) the transaction fee set in BTC/kB
      |  "relayfee": x.xxxx,         (numeric) minimum relay fee for non-free transactions in BTC/kB
      |  "errors": "..."           (string) any error messages
      |}
      |
      |Examples:
      |> bitcoin-cli getinfo
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getinfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}