package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetBlockCount: returns the number of blocks in the local best block chain.
  *
  * https://bitcoin.org/en/developer-reference#getblockcount
  */
object GetBlockCount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


