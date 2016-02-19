package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** ImportAddress: adds an address or pubkey script to the wallet without the associated private key,
  * allowing you to watch for transactions affecting that address or
  * pubkey script without being able to spend any of its outputs.
  *
  * Since - New in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#importaddress
  */
object ImportAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


