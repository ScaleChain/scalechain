package io.scalechain.blockchain.api.command.utility.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

/*
  CLI command :
    bitcoin-cli estimatepriority 6

  CLI output :
    718158904.10958910

  CLI command :
    # Requesting data the node can’t calculate yet.
    bitcoin-cli estimatepriority 100

  CLI output :
    -1.00000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "estimatepriority", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** EstimatePriority: estimates the priority that a transaction needs
  * in order to be included within a certain number of blocks as a free high-priority transaction.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#estimatepriority
  */
object EstimatePriority : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
"""

"""
}


