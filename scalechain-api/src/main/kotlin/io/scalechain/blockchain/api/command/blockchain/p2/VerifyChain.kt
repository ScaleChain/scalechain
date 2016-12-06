package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right


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
object VerifyChain : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """verifychain ( checklevel numblocks )
      |
      |Verifies blockchain database.
      |
      |Arguments:
      |1. checklevel   (numeric, optional, 0-4, default=3) How thorough the block verification is.
      |2. numblocks    (numeric, optional, default=288, 0=all) The number of blocks to check.
      |
      |Result:
      |true|false       (boolean) Verified or not
      |
      |Examples:
      |> bitcoin-cli verifychain
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "verifychain", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
      |
    """.trimMargin()
}


