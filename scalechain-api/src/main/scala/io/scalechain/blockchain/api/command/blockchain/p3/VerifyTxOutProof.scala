package io.scalechain.blockchain.api.command.blockchain.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  Json-RPC request :

  Json-RPC response :

*/

/** VerifyTxOutProof: verifies that a proof points to one or more transactions in a block,
  * returning the transactions the proof commits to
  * and throwing an RPC error if the block is not in our best block chain.
  *
  * Since - New in 0.11.0
  *
  */
object VerifyTxOutProof extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


