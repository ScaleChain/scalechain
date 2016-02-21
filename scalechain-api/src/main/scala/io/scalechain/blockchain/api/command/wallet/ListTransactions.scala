package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

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

case class ListTransactionsResult(
) extends RpcResult


/** ListTransactions: returns the most recent transactions that affect the wallet.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listtransactions
  */
object ListTransactions extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """listtransactions ( "account" count from includeWatchonly)
      |
      |Returns up to 'count' most recent transactions skipping the first 'from' transactions for account 'account'.
      |
      |Arguments:
      |1. "account"    (string, optional) DEPRECATED. The account name. Should be "*".
      |2. count          (numeric, optional, default=10) The number of transactions to return
      |3. from           (numeric, optional, default=0) The number of transactions to skip
      |4. includeWatchonly (bool, optional, default=false) Include transactions to watchonly addresses (see 'importaddress')
      |
      |Result:
      |[
      |  {
      |    "account":"accountname",       (string) DEPRECATED. The account name associated with the transaction.
      |                                                It will be "" for the default account.
      |    "address":"bitcoinaddress",    (string) The bitcoin address of the transaction. Not present for
      |                                                move transactions (category = move).
      |    "category":"send|receive|move", (string) The transaction category. 'move' is a local (off blockchain)
      |                                                transaction between accounts, and not associated with an address,
      |                                                transaction id or block. 'send' and 'receive' transactions are
      |                                                associated with an address, transaction id and block details
      |    "amount": x.xxx,          (numeric) The amount in BTC. This is negative for the 'send' category, and for the
      |                                         'move' category for moves outbound. It is positive for the 'receive' category,
      |                                         and for the 'move' category for inbound funds.
      |    "vout": n,                (numeric) the vout value
      |    "fee": x.xxx,             (numeric) The amount of the fee in BTC. This is negative and only available for the
      |                                         'send' category of transactions.
      |    "confirmations": n,       (numeric) The number of confirmations for the transaction. Available for 'send' and
      |                                         'receive' category of transactions. Negative confirmations indicate the
      |                                         transaction conflicts with the block chain
      |    "trusted": xxx            (bool) Whether we consider the outputs of this unconfirmed transaction safe to spend.
      |    "blockhash": "hashvalue", (string) The block hash containing the transaction. Available for 'send' and 'receive'
      |                                          category of transactions.
      |    "blockindex": n,          (numeric) The block index containing the transaction. Available for 'send' and 'receive'
      |                                          category of transactions.
      |    "blocktime": xxx,         (numeric) The block time in seconds since epoch (1 Jan 1970 GMT).
      |    "txid": "transactionid", (string) The transaction id. Available for 'send' and 'receive' category of transactions.
      |    "time": xxx,              (numeric) The transaction time in seconds since epoch (midnight Jan 1 1970 GMT).
      |    "timereceived": xxx,      (numeric) The time received in seconds since epoch (midnight Jan 1 1970 GMT). Available
      |                                          for 'send' and 'receive' category of transactions.
      |    "comment": "...",       (string) If a comment is associated with the transaction.
      |    "label": "label"        (string) A comment for the address/transaction, if any
      |    "otheraccount": "accountname",  (string) For the 'move' category of transactions, the account the funds came
      |                                          from (for receiving funds, positive amounts), or went to (for sending funds,
      |                                          negative amounts).
      |    "bip125-replaceable": "yes|no|unknown"  (string) Whether this transaction could be replaced due to BIP125 (replace-by-fee);
      |                                                     may be unknown for unconfirmed transactions not in the mempool
      |  }
      |]
      |
      |Examples:
      |
      |List the most recent 10 transactions in the systems
      |> bitcoin-cli listtransactions
      |
      |List transactions 100 to 120
      |> bitcoin-cli listtransactions "*" 20 100
      |
      |As a json rpc call
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "listtransactions", "params": ["*", 20, 100] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


