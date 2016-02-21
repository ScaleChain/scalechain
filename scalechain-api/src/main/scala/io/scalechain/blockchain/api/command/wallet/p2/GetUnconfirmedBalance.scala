package io.scalechain.blockchain.api.command.wallet.p2

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getunconfirmedbalance

  CLI output :
    0.00000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getunconfirmedbalance", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetUnconfirmedBalance: returns the wallet’s total unconfirmed balance.
  *
  * https://bitcoin.org/en/developer-reference#getunconfirmedbalance
  */
object GetUnconfirmedBalance extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """getunconfirmedbalance
      |Returns the server's total unconfirmed balance
    """.stripMargin

}


