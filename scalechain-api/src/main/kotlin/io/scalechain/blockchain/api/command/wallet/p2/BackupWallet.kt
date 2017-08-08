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
    bitcoin-cli -testnet backupwallet /tmp/backup.dat

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "backupwallet", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** BackupWallet: safely copies wallet.dat to the specified file, which can be a directory or a path with filename.
  *
  * https://bitcoin.org/en/developer-reference#backupwallet
  */
object BackupWallet : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String =
    """backupwallet "destination"
      |
      |Safely copies wallet.dat to destination, which can be a directory or a path with filename.
      |
      |Arguments:
      |1. "destination"   (string) The destination directory or file
      |
      |Examples:
      |> bitcoin-cli backupwallet "backup.dat"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "backupwallet", "params": ["backup.dat"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.trimMargin()
}


