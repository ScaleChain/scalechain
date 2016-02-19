package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

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


