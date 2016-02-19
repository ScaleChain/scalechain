package io.scalechain.blockchain.api.command.network.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetConnectionCount: returns the number of connections to other nodes.
  *
  * https://bitcoin.org/en/developer-reference#getconnectioncount
  */
object GetConnectionCount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


