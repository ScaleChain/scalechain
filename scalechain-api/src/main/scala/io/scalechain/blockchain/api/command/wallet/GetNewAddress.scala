package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetNewAddress: returns a new Bitcoin address for receiving payments.
  * If an account is specified, payments received with the address will be credited to that account.
  *
  * https://bitcoin.org/en/developer-reference#getnewaddress
  */
object GetNewAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


