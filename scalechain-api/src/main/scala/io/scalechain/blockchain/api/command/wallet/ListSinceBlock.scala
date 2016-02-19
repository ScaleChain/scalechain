package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ListSinceBlock: gets all transactions affecting the wallet
  * which have occurred since a particular block,
  * plus the header hash of a block at a particular depth.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listsinceblock
  */
object ListSinceBlock extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


