package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


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
object GetBlockCount : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
  """getblockcount
    |
    |Returns the number of blocks in the longest block chain.
    |
    |Result:
    |n    (numeric) The current block count
    |
    |Examples:
    |> bitcoin-cli getblockcount
    |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
  """.trimMargin()
}


