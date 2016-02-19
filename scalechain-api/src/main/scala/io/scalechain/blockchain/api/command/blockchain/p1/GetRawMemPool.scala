package io.scalechain.blockchain.api.command.blockchain.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetRawMemPool: returns all transaction identifiers (TXIDs) in the memory pool as a JSON array,
  * or detailed information about each transaction in the memory pool as a JSON object.
  *
  * https://bitcoin.org/en/developer-reference#getrawmempool
  */
object GetRawMemPool extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


