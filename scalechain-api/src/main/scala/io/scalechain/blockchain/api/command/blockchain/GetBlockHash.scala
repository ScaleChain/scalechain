package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getblockhash 240886

  CLI output :
    00000000a0faf83ab5799354ae9c11da2a2bd6db44058e03c528851dee0a3fff

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getblockhash", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetBlockHash: returns the header hash of a block at the given height in the local best block chain.
  *
  * https://bitcoin.org/en/developer-reference#getblockhash
  */
object GetBlockHash extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


