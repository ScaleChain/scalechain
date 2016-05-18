package io.scalechain.wallet

import io.scalechain.blockchain.proto.Hash

/** Describes details on a transaction.
  */
case class TransactionDescriptor(
  involvesWatchonly : Boolean,       // true,
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
  // P1
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
  // P1
  blockhash         : Option[Hash],          // "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929",
  // Only returned for confirmed transactions.
  // The block height of the block on the local best block chain which includes this transaction
  // P1
  blockindex        : Option[Long],          // 11,
  // Only returned for confirmed transactions.
  // The block header time (Unix epoch time) of the block on the local best block chain which includes this transaction
  // P1
  blocktime         : Option[Long],          // 1411051649,
  // The TXID of the transaction, encoded as hex in RPC byte order. Not returned for move category payments
  txid              : Option[Hash],          // "99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d",
  // An array containing the TXIDs of other transactions that spend the same inputs (UTXOs) as this transaction.
  // Array may be empty. Not returned for move category payments
  // walletconflicts item : The TXID of a conflicting transaction, encoded as hex in RPC byte order
  // P2
  //    walletconflicts   : List[Hash],            // : [],
  // A Unix epoch time when the transaction was added to the wallet
  time              : Long                   // 1418695703,
  // A Unix epoch time when the transaction was detected by the local node,
  // or the time of the block on the local best block chain that included the transaction.
  // Not returned for move category payments
  // P2
  //timereceived      : Option[Long]             // 1418925580
  // For transaction originating with this wallet, a locally-stored comment added to the transaction.
  // Only returned in regular payments if a comment was added.
  // Always returned in move category payments. May be an empty string
  // P3
  //    comment : Option[String],
  // For transaction originating with this wallet, a locally-stored comment added to the transaction
  // identifying who the transaction was sent to.
  // Only returned if a comment-to was added. Never returned by move category payments. May be an empty string
  // P3
  //  to : Option[String],

  // Only returned by move category payments.
  // This is the account the bitcoins were moved from or moved to,
  // as indicated by a negative or positive amount field in this payment
  // P3
  //    otheraccount : Option[String]
)