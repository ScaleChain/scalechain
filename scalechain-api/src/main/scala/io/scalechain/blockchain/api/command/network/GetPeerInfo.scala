package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetPeerInfo: returns data about each connected network node.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#getpeerinfo
  */
object GetPeerInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


