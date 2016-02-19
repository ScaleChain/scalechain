package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** AddMultiSigAddress: adds a P2SH multisig address to the wallet.
  *
  * https://bitcoin.org/en/developer-reference#addmultisigaddress
  */
object AddMultiSigAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


