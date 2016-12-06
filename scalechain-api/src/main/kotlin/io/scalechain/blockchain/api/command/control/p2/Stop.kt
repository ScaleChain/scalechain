package io.scalechain.blockchain.api.command.control.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


/*
  CLI command :
    bitcoin-cli -testnet stop

  CLI output :
    Bitcoin server stopping

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "stop", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** Stop: safely shuts down the Bitcoin Core server.
  *
  * https://bitcoin.org/en/developer-reference#stop
  */
object Stop : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """stop
      |
      |Stop ScaleChain server.
    """.trimMargin()
}


