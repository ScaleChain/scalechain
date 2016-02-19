package io.scalechain.blockchain.api.command.utility.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** EstimatePriority: estimates the priority that a transaction needs
  * in order to be included within a certain number of blocks as a free high-priority transaction.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#estimatepriority
  */
object EstimatePriority extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


