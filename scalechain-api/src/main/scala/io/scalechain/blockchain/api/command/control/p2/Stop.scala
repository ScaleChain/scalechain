package io.scalechain.blockchain.api.command.control.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** Stop: safely shuts down the Bitcoin Core server.
  *
  * https://bitcoin.org/en/developer-reference#stop
  */
object Stop extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


