package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** DumpWallet: creates or overwrites a file with all wallet keys in a human-readable format.
  *
  * https://bitcoin.org/en/developer-reference#dumpwallet
  */
object DumpWallet extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


