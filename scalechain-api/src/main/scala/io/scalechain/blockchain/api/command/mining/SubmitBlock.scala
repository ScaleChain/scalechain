package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** SubmitBlock: accepts a block, verifies it is a valid addition to the block chain, and
  * broadcasts it to the network.
  *
  * Extra parameters are ignored by Bitcoin Core but may be used by mining pools or other programs.
  *
  */
object SubmitBlock extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


