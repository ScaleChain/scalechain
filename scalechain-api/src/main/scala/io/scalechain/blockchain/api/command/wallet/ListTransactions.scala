package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # List the most recent transaction from the account “someone else’s address2” including watch-only addresses.
    bitcoin-cli -testnet listtransactions "someone else's address2" 1 0 true

  CLI output :
    [
        {
            "involvesWatchonly" : true,
            "account" : "someone else's address2",
            "address" : "n3GNqMveyvaPvUbH469vDRadqpJMPc84JA",
            "category" : "receive",
            "amount" : 0.00050000,
            "vout" : 0,
            "confirmations" : 34714,
            "blockhash" : "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929",
            "blockindex" : 11,
            "blocktime" : 1411051649,
            "txid" : "99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d",
            "walletconflicts" : [
            ],
            "time" : 1418695703,
            "timereceived" : 1418925580
        }
    ]

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listtransactions", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ListTransactions: returns the most recent transactions that affect the wallet.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listtransactions
  */
object ListTransactions extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


