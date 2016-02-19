package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Set the transaction fee per kilobyte to 100,000 sc.
    bitcoin-cli -testnet settxfee 0.00100000

  CLI output :
    true

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "settxfee", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** SetTxFee: sets the transaction fee per kilobyte paid by transactions created by this wallet.
  *
  * https://bitcoin.org/en/developer-reference#settxfee
  */
object SetTxFee extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


