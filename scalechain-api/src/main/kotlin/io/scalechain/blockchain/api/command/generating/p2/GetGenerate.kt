package io.scalechain.blockchain.api.command.generating.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


/*
  CLI command :
    bitcoin-cli -testnet getgenerate

  CLI output :
    false

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getgenerate", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetGenerate: returns true if the node is set to generate blocks using its CPU.
  *
  * https://bitcoin.org/en/developer-reference#getgenerate
  */
object GetGenerate : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """getgenerate
      |
      |Return if the server is set to generate coins or not. The default is false.
      |It is set with the command line argument -gen (or bitcoin.conf setting gen)
      |It can also be set with the setgenerate call.
      |
      |Result
      |true|false      (boolean) If the server is set to generate coins or not
      |
      |Examples:
      |> bitcoin-cli getgenerate
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getgenerate", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()

}


