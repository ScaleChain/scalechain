package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ListReceivedByAccount: lists the total number of bitcoins received by each account.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listreceivedbyaccount
  */
object ListReceivedByAccount extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


