package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** LockUnspent: temporarily locks or unlocks specified transaction outputs.
  * A locked transaction output will not be chosen by automatic coin selection when spending bitcoins.
  *
  * Locks are stored in memory only, so nodes start with zero locked outputs and
  * the locked output list is always cleared when a node stops or fails.
  *
  * https://bitcoin.org/en/developer-reference#lockunspent
  */
object LockUnspent extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


