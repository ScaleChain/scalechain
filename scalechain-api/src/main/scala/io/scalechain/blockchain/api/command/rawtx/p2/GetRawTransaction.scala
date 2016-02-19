package io.scalechain.blockchain.api.command.rawtx.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** GetRawTransaction: gets a hex-encoded serialized transaction or a JSON object describing the transaction.
  * By default, Bitcoin Core only stores complete transaction data for UTXOs and your own transactions,
  * so the RPC may fail on historic transactions unless you use the non-default txindex=1 in your Bitcoin Core startup settings.
  *
  * https://bitcoin.org/en/developer-reference#getrawtransaction
  */
object GetRawTransaction extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


