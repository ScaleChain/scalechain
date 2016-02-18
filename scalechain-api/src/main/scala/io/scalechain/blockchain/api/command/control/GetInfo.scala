package io.scalechain.blockchain.api.command.control

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
// Json-RPC request : {"jsonrpc": "1.0", "id":"curltest", "method": "getinfo", "params": [] }
// Json-RPC response :
{
  "result": {
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
  },
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
  */
object GetInfo extends RpcCommand {

  def invoke(request : RpcRequest) : RpcResult = {
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
  }
}