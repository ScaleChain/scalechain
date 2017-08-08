package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

/*
  CLI command :
    bitcoin-cli -testnet importwallet /tmp/dump.txt

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "importwallet", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** ImportWallet: imports private keys from a file in wallet dump file format (see the dumpwallet RPC).
  * These keys will be added to the keys currently in the wallet.
  * This call may need to rescan all or parts of the block chain for transactions affecting the newly-added keys,
  * which may take several minutes.
  *
  * https://bitcoin.org/en/developer-reference#importwallet
  */
object ImportWallet : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """importwallet "filename"
      |
      |Imports keys from a wallet dump file (see dumpwallet).
      |
      |Arguments:
      |1. "filename"    (string, required) The wallet file
      |
      |Examples:
      |
      |Dump the wallet
      |> bitcoin-cli dumpwallet "test"
      |
      |Import the wallet
      |> bitcoin-cli importwallet "test"
      |
      |Import using the json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "importwallet", "params": ["test"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


