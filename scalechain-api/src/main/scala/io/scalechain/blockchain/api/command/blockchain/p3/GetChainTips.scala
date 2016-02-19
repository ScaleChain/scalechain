package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetChainTips: returns information about the highest-height block (tip) of each local block chain.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getchaintips
  */
object GetChainTips extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


