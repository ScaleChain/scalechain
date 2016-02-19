package io.scalechain.blockchain.api.command.network.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** AddNode: attempts to add or remove a node from the addnode list, or to try a connection to a node once.
  *
  * https://bitcoin.org/en/developer-reference#addnode
  */
object AddNode extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


