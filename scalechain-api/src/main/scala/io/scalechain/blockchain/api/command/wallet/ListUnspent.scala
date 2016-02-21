package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Get all outputs confirmed at least 6 times for a particular address.
    bitcoin-cli -testnet listunspent 6 99999999 '''
      [
        "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe"
      ]
    '''

  CLI output :
    [
        {
            "txid" : "d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a",
            "vout" : 1,
            "address" : "mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe",
            "account" : "test label",
            "scriptPubKey" : "76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac",
            "amount" : 0.00010000,
            "confirmations" : 6210,
            "spendable" : true
        }
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listunspent", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ListUnspent: returns an array of unspent transaction outputs belonging to this wallet.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listunspent
  */
object ListUnspent extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


