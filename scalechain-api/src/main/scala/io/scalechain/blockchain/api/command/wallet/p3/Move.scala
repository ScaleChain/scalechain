package io.scalechain.blockchain.api.command.wallet.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Move 0.1 bitcoins from “doc test” to “test1”, giving the transaction the comment “Example move”.
    bitcoin-cli -testnet move "doc test" "test1" 0.1 0 "Example move"

  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "move", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** Move: moves a specified amount from one account in your wallet to another using an off-block-chain transaction.
  *
  * https://bitcoin.org/en/developer-reference#move
  */
object Move extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


