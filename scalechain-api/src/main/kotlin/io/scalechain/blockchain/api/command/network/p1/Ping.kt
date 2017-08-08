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
    bitcoin-cli -testnet ping

  CLI output :
    (Success: no result printed.)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "ping", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** Ping: sends a P2P ping message to all connected nodes to measure ping time.
  *
  * Results are provided by the getpeerinfo RPC pingtime and pingwait fields as decimal seconds.
  * The P2P ping message is handled in a queue with all other commands,
  * so it measures processing backlog, not just network ping.
  *
  */
object Ping : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
"""

"""
}


