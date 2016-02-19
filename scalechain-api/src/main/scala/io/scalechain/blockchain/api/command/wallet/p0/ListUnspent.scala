package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ListUnspent: returns an array of unspent transaction outputs belonging to this wallet.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listunspent
  */
object ListUnspent extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


