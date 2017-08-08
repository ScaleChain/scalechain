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
    bitcoin-cli estimatefee 6

  CLI output :
    0.00026809

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "estimatefee", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** EstimateFee: estimates the transaction fee per kilobyte that needs to be paid for a transaction
  * to be included within a certain number of blocks.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#estimatefee
  */
object EstimateFee : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """estimatefee nblocks
      |
      |Estimates the approximate fee per kilobyte needed for a transaction to begin
      |confirmation within nblocks blocks.
      |
      |Arguments:
      |1. nblocks     (numeric)
      |
      |Result:
      |n              (numeric) estimated fee-per-kilobyte
      |
      |A negative value is returned if not enough transactions and blocks
      |have been observed to make an estimate.
      |
      |Example:
      |> bitcoin-cli estimatefee 6
    """.trimMargin()
}


