package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetReceivedByAccount: returns the total amount received by addresses in a particular account from
  * transactions with the specified number of confirmations. It does not count coinbase transactions.
  *
  */
object GetReceivedByAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


