package io.scalechain.blockchain.api.command.wallet.p0

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

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
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """listsinceblock ( "blockhash" target-confirmations includeWatchonly)
      |
      |Get all transactions in blocks since block [blockhash], or all transactions if omitted
      |
      |Arguments:
      |1. "blockhash"   (string, optional) The block hash to list transactions since
      |2. target-confirmations:    (numeric, optional) The confirmations required, must be 1 or more
      |3. includeWatchonly:        (bool, optional, default=false) Include transactions to watchonly addresses (see 'importaddress')
      |Result:
      |{
      |  "transactions": [
      |    "account":"accountname",       (string) DEPRECATED. The account name associated with the transaction. Will be "" for the default account.
      |    "address":"bitcoinaddress",    (string) The bitcoin address of the transaction. Not present for move transactions (category = move).
      |    "category":"send|receive",     (string) The transaction category. 'send' has negative amounts, 'receive' has positive amounts.
      |    "amount": x.xxx,          (numeric) The amount in BTC. This is negative for the 'send' category, and for the 'move' category for moves
      |                                          outbound. It is positive for the 'receive' category, and for the 'move' category for inbound funds.
      |    "vout" : n,               (numeric) the vout value
      |    "fee": x.xxx,             (numeric) The amount of the fee in BTC. This is negative and only available for the 'send' category of transactions.
      |    "confirmations": n,       (numeric) The number of confirmations for the transaction. Available for 'send' and 'receive' category of transactions.
      |    "blockhash": "hashvalue",     (string) The block hash containing the transaction. Available for 'send' and 'receive' category of transactions.
      |    "blockindex": n,          (numeric) The block index containing the transaction. Available for 'send' and 'receive' category of transactions.
      |    "blocktime": xxx,         (numeric) The block time in seconds since epoch (1 Jan 1970 GMT).
      |    "txid": "transactionid",  (string) The transaction id. Available for 'send' and 'receive' category of transactions.
      |    "time": xxx,              (numeric) The transaction time in seconds since epoch (Jan 1 1970 GMT).
      |    "timereceived": xxx,      (numeric) The time received in seconds since epoch (Jan 1 1970 GMT). Available for 'send' and 'receive' category of transactions.
      |    "comment": "...",       (string) If a comment is associated with the transaction.
      |    "label" : "label"       (string) A comment for the address/transaction, if any
      |    "to": "...",            (string) If a comment to is associated with the transaction.
      |  ],
      |  "lastblock": "lastblockhash"     (string) The hash of the last block
      |}
      |
      |Examples:
      |> bitcoin-cli listsinceblock
      |> bitcoin-cli listsinceblock "000000000000000bacf66f7497b7dc45ef753ee9a7d38571037cdb1a57f663ad" 6
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "listsinceblock", "params": ["000000000000000bacf66f7497b7dc45ef753ee9a7d38571037cdb1a57f663ad", 6] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


