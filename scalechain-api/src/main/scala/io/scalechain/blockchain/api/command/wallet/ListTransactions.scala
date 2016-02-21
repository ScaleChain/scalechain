package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}
import io.scalechain.blockchain.proto.Hash

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

case class TransactionItem(
  involvesWatchonly : Option[Boolean],       // true,
  // The account which the payment was credited to or debited from.
  // May be an empty string (“”) for the default account
  account           : String,                // "someone else's address2",
  // The address paid in this payment, which may be someone else’s address not belonging to this wallet.
  // May be empty if the address is unknown, such as when paying to a non-standard pubkey script or if this is in the move category
  address           : Option[String],        // "n3GNqMveyvaPvUbH469vDRadqpJMPc84JA",
  // Set to one of the following values:
  // • send if sending payment
  // • receive if this wallet received payment in a regular transaction
  // • generate if a matured and spendable coinbase
  // • immature if a coinbase that is not spendable yet
  // • orphan if a coinbase from a block that’s not in the local best block chain
  // • move if an off-block-chain move made with the move RPC
  category          : String,                // "receive",
  // A negative bitcoin amount if sending payment;
  // a positive bitcoin amount if receiving payment (including coinbases)
  amount            : scala.math.BigDecimal, // 0.00050000,
  // ( Since : 0.10.0 )
  // For an output, the output index (vout) for this output in this transaction.
  // For an input, the output index for the output being spent in its transaction.
  // Because inputs list the output indexes from previous transactions,
  // more than one entry in the details array may have the same output index.
  // Not returned for move category payments
  vout              : Option[Int],           // 0,
  // If sending payment, the fee paid as a negative bitcoins value.
  // May be 0. Not returned if receiving payment or for move category payments
  fee               : Option[scala.math.BigDecimal],
  // The number of confirmations the transaction has received.
  // Will be 0 for unconfirmed and -1 for conflicted. Not returned for move category payments
  confirmations     : Option[Long],          // 34714,
  // Set to true if the transaction is a coinbase. Not returned for regular transactions or move category payments
  generated         : Option[Boolean],
  // Only returned for confirmed transactions.
  // The hash of the block on the local best block chain which includes this transaction, encoded as hex in RPC byte order
  blockhash         : Option[Hash],          // "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929",
  // Only returned for confirmed transactions.
  // The block height of the block on the local best block chain which includes this transaction
  blockindex        : Option[Long],          // 11,
  // Only returned for confirmed transactions.
  // The block header time (Unix epoch time) of the block on the local best block chain which includes this transaction
  blocktime         : Option[Long],          // 1411051649,
  // The TXID of the transaction, encoded as hex in RPC byte order. Not returned for move category payments
  txid              : Option[Hash],          // "99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d",
  // An array containing the TXIDs of other transactions that spend the same inputs (UTXOs) as this transaction.
  // Array may be empty. Not returned for move category payments
  // walletconflicts item : The TXID of a conflicting transaction, encoded as hex in RPC byte order
  walletconflicts   : List[Hash],            // : [],
  // A Unix epoch time when the transaction was added to the wallet
  time              : Long,                  // 1418695703,
  // A Unix epoch time when the transaction was detected by the local node,
  // or the time of the block on the local best block chain that included the transaction.
  // Not returned for move category payments
  timereceived      : Option[Long],          // 1418925580
  // For transaction originating with this wallet, a locally-stored comment added to the transaction.
  // Only returned in regular payments if a comment was added.
  // Always returned in move category payments. May be an empty string
  comment : Option[String],
  // For transaction originating with this wallet, a locally-stored comment added to the transaction
  // identifying who the transaction was sent to.
  // Only returned if a comment-to was added. Never returned by move category payments. May be an empty string
  to : Option[String],

  // Only returned by move category payments.
  // This is the account the bitcoins were moved from or moved to,
  // as indicated by a negative or positive amount field in this payment
  otheraccount : Option[String]
)

case class ListTransactionsResult( transactionItems : List[TransactionItem] ) extends RpcResult


/** ListTransactions: returns the most recent transactions that affect the wallet.
  *
  * Updated in 0.10.0
  *
  * https://bitcoin.org/en/developer-reference#listtransactions
  */
object ListTransactions extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]] = {
    // TODO : Implement

    // An array containing objects, with each object describing a payment or internal accounting entry (not a transaction).
    // More than one object in this array may come from a single transaction. Array may be empty
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

    Right( Some( ListTransactionsResult(transactionItems) ) )
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


