package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Verify the most recent 10,000 blocks in the most through way:
    bitcoin-cli -testnet verifychain 4 10000

  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "verifychain", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** VerifyChain: verifies each entry in the local block chain database.
  *
  * https://bitcoin.org/en/developer-reference#verifychain
  */
object VerifyChain extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


