package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getblockchaininfo

  CLI output :
    {
        "chain" : "test",
        "blocks" : 315280,
        "headers" : 315280,
        "bestblockhash" : "000000000ebb17fb455e897b8f3e343eea1b07d926476d00bc66e2c0342ed50f",
        "difficulty" : 1.00000000,
        "verificationprogress" : 1.00000778,
        "chainwork" : "0000000000000000000000000000000000000000000000015e984b4fb9f9b350"
    }
  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblockchaininfo", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }

*/

/** GetBlockChainInfo: provides information about the current state of the block chain.
  *
  * Since - New in 0.9.2, Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getblockchaininfo
  */
object GetBlockChainInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


