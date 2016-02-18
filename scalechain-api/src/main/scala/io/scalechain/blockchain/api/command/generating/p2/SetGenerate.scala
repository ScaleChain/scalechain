package io.scalechain.blockchain.api.command.generating.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** SetGenerate: enables or disables hashing to attempt to find the next block.
  *
  * Since - Updated in master
  *
  */
object SetGenerate extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


