package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** Help: lists all available public RPC commands, or gets help for the specified RPC.
  *
  * Commands which are unavailable will not be listed, such as wallet RPCs if wallet support is disabled.
  *
  * https://bitcoin.org/en/developer-reference#help
  */
object Help extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


