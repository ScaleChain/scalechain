package io.scalechain.blockchain.proto

import java.math.BigInteger

import io.scalechain.blockchain.*
import io.scalechain.util.HexUtil
import io.scalechain.util.Option

/** data classes that are used for keys or values of the wallet database.
  */

/** An account with a name.
  *
  * @param name The account name.
  */
data class Account(val name : String) : Transcodable


/** An outpoint points to an output in a transaction.
  *
  * @param transactionHash The hash of the transaction that has the output.
  * @param outputIndex The index of the output. The index starts from 0. Ex> The first output of a transaction has index 0.
  */
data class OutPoint(val transactionHash : Hash, val outputIndex : Int) : Transcodable


/** An in point points to an input in a transaction.
  *
  * @param transactionHash The hash of the transaction that has the output.
  * @param inputIndex The index of the input. The index starts from 0. Ex> The first input of a transaction has index 0.
  */
data class InPoint(val transactionHash : Hash, val inputIndex : Int) : Transcodable


/**
 *  Wallet Transaction Attribute ; Set to one of the following values:
 *  • send if sending payment
 *  • receive if this wallet received payment in a regular transaction
 *  • generate if a matured and spendable coinbase
 *  • immature if a coinbase that is not spendable yet
 *  • orphan if a coinbase from a block that’s not in the local best block chain
 *  • move if an off-block-chain move made with the move RPC
 */
/*
object WalletTransactionAttribute : Enumeration {
  type WalletTransactionAttribute = Value

/*
  val SEND     = Val(nextId, "send")
  val RECEIVE  = Val(nextId, "receive")
  val GENERATE = Val(nextId, "generate")
  val IMMATURE = Val(nextId, "immature")
  val ORPHAN   = Val(nextId, "orphan")
  val MOVE     = Val(nextId, "move")
*/

  val SEND     = Value
  val RECEIVE  = Value
  val GENERATE = Value
  val IMMATURE = Value
  val ORPHAN   = Value
  val MOVE     = Value

}
*/
/** A transaction stored for an output ownership.
  */
data class WalletTransaction(
                              // Only returned for confirmed transactions.
                              // The hash of the block on the local best block chain which includes this transaction, encoded as hex in RPC byte order
                              // P1
                              val blockHash         : Option<Hash>, // "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929",
                              // Only returned for confirmed transactions.
                              // The block height of the block on the local best block chain which includes this transaction
                              // P1
                              val blockIndex        : Option<Long>, // 11,
                              // Only returned for confirmed transactions.
                              // The block header time (Unix epoch time) of the block on the local best block chain which includes this transaction
                              // P1
                              val blockTime         : Option<Long>, // 1411051649,
                              // The TXID of the transaction, encoded as hex in RPC byte order. Not returned for move category payments
                              val transactionId     : Option<Hash>, // "99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d",
                              // An array containing the TXIDs of other transactions that spend the same inputs (UTXOs) as this transaction.
                              // Array may be empty. Not returned for move category payments
                              // walletconflicts item : The TXID of a conflicting transaction, encoded as hex in RPC byte order
                              // P2
                              //    walletconflicts   : List<Hash>,            // : [],
                              // A Unix epoch time when the transaction was added to the wallet
                              val addedTime              : Long, // 1418695703,
                              // A Unix epoch time when the transaction was detected by the local node,
                              // or the time of the block on the local best block chain that included the transaction.
                              // Not returned for move category payments
                              // P2
                              //    timereceived      : Option<Long>,          // 1418925580

                              // An additional field for sorting transactions by recency.
                              // Some(transactionIndex) if the transaction is in a block on the best blockchain.
                              // None if the block is in the mempool.
                              val transactionIndex : Option<Int>,

                              // The transaction related with this wallet transaction.
                              val transaction : Transaction
) : Transcodable

/** Ownership is described as multiple private keys.
  *
  * TODO : Implement multiple private keys.
  *
  * @param privateKeys
  */
data class OwnershipDescriptor(val account : String, val privateKeys : List<String>) : Transcodable

data class WalletOutput(
    // Some(block height) of the block on the local best block chain which includes this transaction. None otherwise.
    // BUGBUG : Change name to blockIndex
    val blockindex : Option<Long>,          // 11,
    // Whether this output is in the generation transaction.
    val coinbase : Boolean,
    // Whether this coin was spent or not.
    val spent : Boolean,
    // The transaction output
    val transactionOutput : TransactionOutput
) : Transcodable
