package io.scalechain.blockchain.api.command.rawtx.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** CreateRawTransaction: creates an unsigned serialized transaction that spends a previous output
  * to a new output with a P2PKH or P2SH address.
  *
  * The transaction is not stored in the wallet or transmitted to the network.
  *
  * https://bitcoin.org/en/developer-reference#createrawtransaction
  */
object CreateRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


