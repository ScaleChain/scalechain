package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetWalletInfo: provides information about the wallet.
  *
  * Since - New in 0.9.2
  *
  * https://bitcoin.org/en/developer-reference#getwalletinfo
  */
object GetWalletInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


