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
    # Unlock the wallet for 10 minutes (the passphrase is “test”).
    bitcoin-cli -testnet walletpassphrase test 600

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "walletpassphrase", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** WalletPassphrase: stores the wallet decryption key in memory for the indicated number of seconds.
  * Issuing the walletpassphrase command while the wallet is already unlocked will set a new unlock time that overrides the old one.
  *
  * https://bitcoin.org/en/developer-reference#walletpassphrase
  */
object WalletPassphrase : RpcCommand() {
  override fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?> {
    // TODO : Implement
    assert(false)
    return Right(null)
  }
  override fun help() : String = ""
}


