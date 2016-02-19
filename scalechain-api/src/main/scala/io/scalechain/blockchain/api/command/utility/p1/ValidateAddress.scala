package io.scalechain.blockchain.api.command.utility.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ValidateAddress: returns information about the given Bitcoin address.
  *
  * https://bitcoin.org/en/developer-reference#validateaddress
  */
object ValidateAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


