package io.scalechain.blockchain.api.command.rawtx.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** SignRawTransaction: signs a transaction in the serialized transaction format
  * using private keys stored in the wallet or provided in the call.
  *
  * https://bitcoin.org/en/developer-reference#signrawtransaction
  */
object SignRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


