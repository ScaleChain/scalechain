package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getblockcount

  CLI output :
    315280

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetBlockCount: returns the number of blocks in the local best block chain.
  *
  * https://bitcoin.org/en/developer-reference#getblockcount
  */
object GetBlockCount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


