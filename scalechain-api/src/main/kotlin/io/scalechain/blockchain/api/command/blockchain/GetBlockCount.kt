package io.scalechain.blockchain.api.command.blockchain

import io.scalechain.blockchain.net.RpcSubSystem
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.NumberResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.util.Either
import io.scalechain.util.Either.Right
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import java.math.BigDecimal

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
  override fun invoke(request: RpcRequest): Either<RpcError, RpcResult?> {
    return handlingException {
      val blockCount: Long = RpcSubSystem.get().getBestBlockHeight()
      Right(NumberResult(BigDecimal.valueOf(blockCount)))
    }
  }

  override fun help() : String =
    """getblockcount
      |
      |Returns the number of blocks in the local best block chain.
      |
      |Result
      |(int)    The number of blocks in the local best block chain.
      |
      |Examples:
      |> bitcoin-cli -testnet getblockcount
      |> curl --data-binary '{"jsonrpc": "1.0", "id":1, "method": "getblockcount", "params": [] }' -H
      'Content-Type: application/json' http://127.0.0.1:8332/
    """.trimMargin()
}