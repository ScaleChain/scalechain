package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetBlock: gets a block with a particular header hash
  * from the local block database either as a JSON object or as a serialized block.
  *
  * https://bitcoin.org/en/developer-reference#getblock
  */
object GetBlock extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


