package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetBalance: gets the balance in decimal bitcoins across all accounts or for a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getbalance
  */
object GetBalance extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


