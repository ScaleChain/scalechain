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
    bitcoin-cli -regtest generate 2

  CLI output :
    [
        "36252b5852a5921bdfca8701f936b39edeb1f8c39fffe73b0d8437921401f9af",
        "5f2956817db1e386759aa5794285977c70596b39ea093b9eab0aa4ba8cd50c06"
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "generate", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** Generate: nearly instantly generates blocks (in regtest mode only)
  *
  * Since - New in master
  *
  * https://bitcoin.org/en/developer-reference#generate
  */
object Generate : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """generate numblocks
      |
      |Mine blocks immediately (before the RPC call returns)
      |
      |Note: this function can only be used on the regtest network
      |
      |Arguments:
      |1. numblocks    (numeric, required) How many blocks are generated immediately.
      |
      |Result
      |[ blockhashes ]     (array) hashes of blocks generated
      |
      |Examples:
      |
      |Generate 11 blocks
      |> bitcoin-cli generate 11
    """.trimMargin()
}


