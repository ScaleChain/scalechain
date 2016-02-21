package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Change the wallet passphrase from “test” to “example”.
    bitcoin-cli -testnet walletpassphrasechange test example

  CLI output :
    (no output)

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "walletpassphrasechange", "params": [] }

  Json-RPC response :
    {
      "result": null,
      "error": null,
      "id": "curltest"
    }
*/

/** WalletPassphraseChange: changes the wallet passphrase from ‘old passphrase’ to ‘new passphrase’.
  *
  * https://bitcoin.org/en/developer-reference#walletpassphrasechange
  */
object WalletPassphraseChange extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String = ""
}


