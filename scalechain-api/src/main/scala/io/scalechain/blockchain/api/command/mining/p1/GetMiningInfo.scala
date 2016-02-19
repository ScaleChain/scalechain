package io.scalechain.blockchain.api.command.mining.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetMiningInfo: returns various mining-related information.
  *
  * Updated in master
  *
  * https://bitcoin.org/en/developer-reference#getmininginfo
  */
object GetMiningInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


