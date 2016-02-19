package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** VerifyChain: verifies each entry in the local block chain database.
  *
  * https://bitcoin.org/en/developer-reference#verifychain
  */
object VerifyChain extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


