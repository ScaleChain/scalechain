package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.domain.{RpcRequest, RpcResult}

/*
  CLI command :
    # Get all transactions since a particular block (including watch-only transactions)
    # and the header hash of the sixth most recent block.
    bitcoin-cli -testnet listsinceblock \
      00000000688633a503f69818a70eac281302e9189b1bb57a76a05c329fcda718 \
      6 true

  CLI output :
    {
        "transactions" : [
            {
                "account" : "doc test",
                "address" : "mmXgiR6KAhZCyQ8ndr2BCfEq1wNG2UnyG6",
                "category" : "receive",
                "amount" : 0.10000000,
                "vout" : 0,
                "confirmations" : 76478,
                "blockhash" : "000000000017c84015f254498c62a7c884a51ccd75d4dd6dbdcb6434aa3bd44d",
                "blockindex" : 1,
                "blocktime" : 1399294967,
                "txid" : "85a98fdf1529f7d5156483ad020a51b7f3340e47448cf932f470b72ff01a6821",
                "walletconflicts" : [
                ],
                "time" : 1399294967,
                "timereceived" : 1418924714
            },
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
        ],
        "lastblock" : "0000000000984add1a686d513e66d25686572c7276ec3e358a7e3e9f7eb88619"
    }

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "listsinceblock", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

/** ListSinceBlock: gets all transactions affecting the wallet
  * which have occurred since a particular block,
  * plus the header hash of a block at a particular depth.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listsinceblock
  */
object ListSinceBlock extends RpcCommand {
  def invoke(request : RpcRequest ) : RpcResult = {
    // TODO : Implement
    assert(false)
    null
  }
}


