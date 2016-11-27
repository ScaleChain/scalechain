package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult

/*
  CLI command :
    bitcoin-cli -testnet walletlock

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "walletlock", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** WalletLock: removes the wallet encryption key from memory, locking the wallet.
  *
  * After calling this method, you will need to call walletpassphrase again
  * before being able to call any methods which require the wallet to be unlocked.
  *
  * https://bitcoin.org/en/developer-reference#walletlock
  */
object WalletLock : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String = ""
}


