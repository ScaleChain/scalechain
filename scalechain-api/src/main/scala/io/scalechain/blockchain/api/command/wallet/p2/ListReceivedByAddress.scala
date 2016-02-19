package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ListReceivedByAddress: lists the total number of bitcoins received by each address.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listreceivedbyaddress
  */
object ListReceivedByAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


