package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** SendFrom: spends an amount from a local account to a bitcoin address.
  *
  * https://bitcoin.org/en/developer-reference#sendfrom
  */
object SendFrom extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


