package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetAddedNodeInfo: returns information about the given added node, or
  * all added nodes (except onetry nodes).
  *
  * Only nodes which have been manually added using the addnode RPC will have their information displayed.
  *
  * https://bitcoin.org/en/developer-reference#getaddednodeinfo
  */
object GetAddedNodeInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


