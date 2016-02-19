package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetUnconfirmedBalance: returns the wallet’s total unconfirmed balance.
  *
  * https://bitcoin.org/en/developer-reference#getunconfirmedbalance
  */
object GetUnconfirmedBalance extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


