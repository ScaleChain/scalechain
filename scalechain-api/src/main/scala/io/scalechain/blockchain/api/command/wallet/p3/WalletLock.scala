package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** WalletLock: removes the wallet encryption key from memory, locking the wallet.
  *
  * After calling this method, you will need to call walletpassphrase again
  * before being able to call any methods which require the wallet to be unlocked.
  *
  */
object WalletLock extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


