package io.scalechain.blockchain.api.command.utility.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** VerifyMessage: verifies a signed message.
  *
  * https://bitcoin.org/en/developer-reference#verifymessage
  */
object VerifyMessage extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


