package io.scalechain.blockchain.api.command.utility.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** CreateMultiSig: creates a P2SH multi-signature address.
  *
  * https://bitcoin.org/en/developer-reference#createmultisig
  */
object CreateMultiSig extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


