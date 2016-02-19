package io.scalechain.blockchain.api.command.rawtx.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** SendRawTransaction: validates a transaction and broadcasts it to the peer-to-peer network.
  *
  * https://bitcoin.org/en/developer-reference#sendrawtransaction
  */
object SendRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


