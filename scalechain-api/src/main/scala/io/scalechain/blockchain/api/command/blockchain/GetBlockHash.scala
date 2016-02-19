package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetBlockHash: returns the header hash of a block at the given height in the local best block chain.
  *
  * https://bitcoin.org/en/developer-reference#getblockhash
  */
object GetBlockHash extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


