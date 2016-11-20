package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.{ErrorCode, UnsupportedFeature}
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.rawtx.GetRawTransaction._
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.{Transaction, HashFormat, Hash}
import io.scalechain.wallet.Wallet
import io.scalechain.wallet.WalletTransactionDescriptor
import spray.json.DefaultJsonProtocol._

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
    {"jsonrpc": "1.0", "id":"curltest", "method": "listtransactions", "params": ["someone else's address2", 1, 0, true] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

data class ListTransactionsResult( transactionDescs : List<WalletTransactionDescriptor> ) : RpcResult


/** ListTransactions: returns the most recent transactions that affect the wallet.
  *
  * Updated in 0.10.0
  *
  * Parameter #1 : Account (String, Optional)
  *   The name of an account to get transactinos from. Use an empty string (“”) to get transactions for the default account. Default is * to get transactions for all accounts
  *
  * Parameter #2 : Count (Number;int, Optional)
  *   The number of the most recent transactions to list. Default is 10.
  *
  * Parameter #3 : Skip (Number;int, Optional)
  *   The number of the most recent transactions which should not be returned.
  *   Allows for pagination of results. Default is 0.
  *
  * Parameter #4 : Include WatchOnly (Boolean, Optional)
  *   ( Since : 0.10.0 )
  *   If set to true, include watch-only addresses in details
  *   and calculations as if they were regular addresses belonging to the wallet.
  *
  *   If set to false (the default), treat watch-only addresses as if they didn’t belong to this wallet.
  *
  * Result: (Array)
  *   An array containing objects, with each object describing a payment or
  *   internal accounting entry (not a transaction).
  *
  *   More than one object in this array may come from a single transaction. Array may be empty.
  *
  * https://bitcoin.org/en/developer-reference#listtransactions
  */
object ListTransactions : RpcCommand {
  fun invoke(request : RpcRequest) : Either<RpcError, Option<RpcResult>> {

    handlingException {
      val account         : String  = request.params.getOption<String> ("Account", 0).getOrElse("")
      val count           : Int     = request.params.getOption<Int>    ("Count", 1).getOrElse(10)
      val skip            : Long    = request.params.getOption<Long>   ("Skip", 2).getOrElse(0)
      val includeWatchOnly: Boolean = request.params.getOption<Boolean>("Include WatchOnly", 3).getOrElse(false)

      // None means to list transactions from all accounts in the wallet.
      val accountOption = if (account == "*") None else Some(account)
      val transactionDescs : List<WalletTransactionDescriptor> = Wallet.get.listTransactions(
        Blockchain.get, accountOption, count, skip, includeWatchOnly
      )(Blockchain.get.db)

      // transactionDescs is an array containing objects, with each object describing a payment or internal accounting entry (not a transaction).
      // More than one object in this array may come from a single transaction. Array may be empty
      /*
      val transactionItems =
        List(
          TransactionItem(
            involvesWatchonly = Some(true),
            account = "someone else's address2",
            address = Some("n3GNqMveyvaPvUbH469vDRadqpJMPc84JA"),
            category = "receive",
            amount = 0.00050000,
            vout = Some(0),
            fee = Some(0.0002),
            confirmations = Some(34714),
            generated = None,
            blockhash = Some(Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
            blockindex = Some(11),
            blocktime = Some(1411051649),
            txid = Some(Hash("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
            List(),
            time = 1418695703,
            timereceived = Some(1418925580),
            comment = None,
            to = None,
            otheraccount = None
          )
        )
      */
      Right(Some(ListTransactionsResult(transactionDescs)))
    }
  }
  fun help() : String =
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


