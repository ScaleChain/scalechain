package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

/*
  CLI command :
    bitcoin-cli -testnet getunconfirmedbalance

  CLI output :
    0.00000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getunconfirmedbalance", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetUnconfirmedBalance: returns the wallet’s total unconfirmed balance.
  *
  * https://bitcoin.org/en/developer-reference#getunconfirmedbalance
  */
object GetUnconfirmedBalance : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """getunconfirmedbalance
      |Returns the server's total unconfirmed balance
    """.trimMargin()

}


