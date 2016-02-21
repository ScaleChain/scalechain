package io.scalechain.blockchain.api.command.mining.p3

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getnetworkhashps -1 227255

  CLI output :
    79510076167

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getnetworkhashps", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetNetworkHashPS: returns the estimated current or historical network hashes per second based on the last n blocks.
  *
  * https://bitcoin.org/en/developer-reference#getnetworkhashps
  */
object GetNetworkHashPS extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement
    assert(false)
    Right(None)
  }
  def help() : String =
    """prioritisetransaction <txid> <priority delta> <fee delta>
      |Accepts the transaction into mined blocks at a higher (or lower) priority
      |
      |Arguments:
      |1. "txid"       (string, required) The transaction id.
      |2. priority delta (numeric, required) The priority to add or subtract.
      |                  The transaction selection algorithm considers the tx as it would have a higher priority.
      |                  (priority of a transaction is calculated: coinage * value_in_satoshis / txsize)
      |3. fee delta      (numeric, required) The fee value (in satoshis) to add (or subtract, if negative).
      |                  The fee is not actually paid, only the algorithm for selecting transactions into a block
      |                  considers the transaction as it would have paid a higher (or lower) fee.
      |
      |Result
      |true              (boolean) Returns true
      |
      |Examples:
      |> bitcoin-cli prioritisetransaction "txid" 0.0 10000
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "prioritisetransaction", "params": ["txid", 0.0, 10000] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


