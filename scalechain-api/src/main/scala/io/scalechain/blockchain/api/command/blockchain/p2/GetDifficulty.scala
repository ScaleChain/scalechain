package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getdifficulty

  CLI output :
    1.00000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getdifficulty", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetDifficulty: returns the proof-of-work difficulty as a multiple of the minimum difficulty.
  *
  * https://bitcoin.org/en/developer-reference#getdifficulty
  */
object GetDifficulty extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


