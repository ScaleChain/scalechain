package io.scalechain.blockchain.api.command.wallet.p1

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

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
object SetTxFee : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  fun help() : String =
    """settxfee amount
      |
      |Set the transaction fee per kB. Overwrites the paytxfee parameter.
      |
      |Arguments:
      |1. amount         (numeric or sting, required) The transaction fee in BTC/kB
      |
      |Result
      |true|false        (boolean) Returns true if successful
      |
      |Examples:
      |> bitcoin-cli settxfee 0.00001
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "settxfee", "params": [0.00001] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


