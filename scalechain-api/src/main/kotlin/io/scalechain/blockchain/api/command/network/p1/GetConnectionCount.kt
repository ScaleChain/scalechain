package io.scalechain.blockchain.api.command.network.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


/*
  CLI command :
    bitcoin-cli -testnet getconnectioncount

  CLI output :
    14

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getconnectioncount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetConnectionCount: returns the number of connections to other nodes.
  *
  * https://bitcoin.org/en/developer-reference#getconnectioncount
  */
object GetConnectionCount : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """getconnectioncount
      |
      |Returns the number of connections to other nodes.
      |
      |Result:
      |n          (numeric) The connection count
      |
      |Examples:
      |> bitcoin-cli getconnectioncount
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getconnectioncount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


