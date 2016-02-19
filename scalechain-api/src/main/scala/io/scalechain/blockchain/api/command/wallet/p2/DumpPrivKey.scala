package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** DumpPrivKey: returns the wallet-import-format (WIP) private key corresponding to an address.
  * (But does not remove it from the wallet.)
  *
  * https://bitcoin.org/en/developer-reference#dumpprivkey
  */
object DumpPrivKey extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


