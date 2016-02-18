package io.scalechain.blockchain.api.command.mining.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetNetworkHashPS: returns the estimated current or historical network hashes per second based on the last n blocks.
  *
  */
object GetNetworkHashPS extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


