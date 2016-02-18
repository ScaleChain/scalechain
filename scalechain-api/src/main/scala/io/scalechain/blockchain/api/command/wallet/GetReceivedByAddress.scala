package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetReceivedByAddress: returns the total amount received by the specified address
  * in transactions with the specified number of confirmations.
  * It does not count coinbase transactions.
  *
  */
object GetReceivedByAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


