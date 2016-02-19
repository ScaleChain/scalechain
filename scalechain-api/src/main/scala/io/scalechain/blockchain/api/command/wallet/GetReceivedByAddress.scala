package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Get the coins received for a particular address, only counting transactions with six or more confirmations.
    bitcoin-cli -testnet getreceivedbyaddress mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN 6

  CLI output :
    0.30000000

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getreceivedbyaddress", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** GetReceivedByAddress: returns the total amount received by the specified address
  * in transactions with the specified number of confirmations.
  * It does not count coinbase transactions.
  *
  * https://bitcoin.org/en/developer-reference#getreceivedbyaddress
  */
object GetReceivedByAddress extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


