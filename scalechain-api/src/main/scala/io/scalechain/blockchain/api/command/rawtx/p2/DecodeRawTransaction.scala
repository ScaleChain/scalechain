package io.scalechain.blockchain.api.command.rawtx.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** DecodeRawTransaction: decodes a serialized transaction hex string into a JSON object describing the transaction.
  *
  * https://bitcoin.org/en/developer-reference#decoderawtransaction
  */
object DecodeRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


