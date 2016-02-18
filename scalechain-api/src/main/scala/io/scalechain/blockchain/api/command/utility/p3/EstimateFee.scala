package io.scalechain.blockchain.api.command.utility.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** EstimateFee: estimates the transaction fee per kilobyte that needs to be paid for a transaction
  * to be included within a certain number of blocks.
  *
  * Since - New in 0.10.0
  *
  */
object EstimateFee extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


