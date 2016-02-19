package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetTxOutProof: returns a hex-encoded proof that one or more specified transactions were included in a block.
  *
  * Since - New in 0.11.0
  *
  * https://bitcoin.org/en/developer-reference#gettxoutproof
  */
object GetTxOutProof extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


