package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ListAddressGroupings: lists groups of addresses that
  * may have had their common ownership made public by common use as inputs in the same transaction or
  * from being used as change from a previous transaction.
  *
  * https://bitcoin.org/en/developer-reference#listaddressgroupings
  */
object ListAddressGroupings extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


