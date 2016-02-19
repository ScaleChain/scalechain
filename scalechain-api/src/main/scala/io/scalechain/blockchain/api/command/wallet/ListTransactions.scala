package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ListTransactions: returns the most recent transactions that affect the wallet.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listtransactions
  */
object ListTransactions extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


