package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetBestBlockHash: returns the header hash of the most recent block on the best block chain.
  *
  * Since - New in 0.9.0
  *
  * https://bitcoin.org/en/developer-reference#getbestblockhash
  */
object GetBestBlockHash extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


