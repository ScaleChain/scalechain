package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetAddressesByAccount: returns a list of every address assigned to a particular account.
  *
  * https://bitcoin.org/en/developer-reference#getaddressesbyaccount
  */
object GetAddressesByAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


