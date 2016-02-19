package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** WalletPassphrase: stores the wallet decryption key in memory for the indicated number of seconds.
  * Issuing the walletpassphrase command while the wallet is already unlocked will set a new unlock time that overrides the old one.
  *
  * https://bitcoin.org/en/developer-reference#walletpassphrase
  */
object WalletPassphrase extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


