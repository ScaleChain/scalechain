package io.scalechain.blockchain.api.command.generating.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Using regtest mode, generate 2 blocks:
    bitcoin-cli -regtest setgenerate true 101

  CLI output :
    [
        "7e38de938d0dcbb41be63d78a8353e77e9d1b3ef82e0368eda051d4926eef915",
        "61d6e5f1a64d009659f45ef1c614e57f4aa0501708641212be236dc56d726da8"
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "setgenerate", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SetGenerate: enables or disables hashing to attempt to find the next block.
  *
  * Since - Updated in master
  *
  * https://bitcoin.org/en/developer-reference#setgenerate
  */
object SetGenerate extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


