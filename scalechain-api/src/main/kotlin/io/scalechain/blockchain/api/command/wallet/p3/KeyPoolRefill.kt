package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

/*
  CLI command :
    # Generate one extra key than the default.
    bitcoin-cli -testnet keypoolrefill 101

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "keypoolrefill", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** KeyPoolRefill: fills the cache of unused pre-generated keys (the keypool).
  *
  * https://bitcoin.org/en/developer-reference#keypoolrefill
  */
object KeyPoolRefill : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """keypoolrefill ( newsize )
      |
      |Fills the keypool.
      |
      |Arguments
      |1. newsize     (numeric, optional, default=100) The keypool size
      |
      |Examples:
      |> bitcoin-cli keypoolrefill
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "keypoolrefill", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


