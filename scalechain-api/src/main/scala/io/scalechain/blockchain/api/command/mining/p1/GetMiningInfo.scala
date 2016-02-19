package io.scalechain.blockchain.api.command.mining.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getmininginfo

  CLI output :
    {
        "blocks" : 313168,
        "currentblocksize" : 1819,
        "currentblocktx" : 3,
        "difficulty" : 1.00000000,
        "errors" : "",
        "genproclimit" : 1,
        "networkhashps" : 5699977416637,
        "pooledtx" : 8,
        "testnet" : true,
        "chain" : "test",
        "generate" : true,
        "hashespersec" : 921200
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getmininginfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetMiningInfo: returns various mining-related information.
  *
  * Updated in master
  *
  * https://bitcoin.org/en/developer-reference#getmininginfo
  */
object GetMiningInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


