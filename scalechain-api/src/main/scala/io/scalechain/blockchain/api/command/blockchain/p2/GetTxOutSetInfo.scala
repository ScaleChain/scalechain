package io.scalechain.blockchain.api.command.blockchain.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetTxOutSetInfo: returns statistics about the confirmed unspent transaction output (UTXO) set.
  * Note that this call may take some time and that it only counts outputs from confirmed transactions.
  * it does not count outputs from the memory pool.
  *
  * https://bitcoin.org/en/developer-reference#gettxoutsetinfo
  */
object GetTxOutSetInfo extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


