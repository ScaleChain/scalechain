package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet encryptwallet "test"

  CLI output :
    wallet encrypted; Bitcoin server stopping, restart to run with encrypted
    wallet. The keypool has been flushed, you need to make a new backup.

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "encryptwallet", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** EncryptWallet: encrypts the wallet with a passphrase.
  * This is only to enable encryption for the first time.
  * After encryption is enabled, you will need to enter the passphrase to use private keys.
  *
  * https://bitcoin.org/en/developer-reference#encryptwallet
  */
object EncryptWallet extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


