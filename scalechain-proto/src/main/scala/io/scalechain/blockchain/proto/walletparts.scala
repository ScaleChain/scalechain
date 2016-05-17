package io.scalechain.blockchain.proto

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.proto.WalletTransactionAttribute.WalletTransactionAttribute
import io.scalechain.util.HexUtil

/** case classes that are used for keys or values of the wallet database.
  */

/** An outpoint points to an output in a transaction.
  *
  * @param transactionHash The hash of the transaction that has the output.
  * @param outputIndex The index of the output. The index starts from 0. Ex> The first output of a transaction has index 0.
  */
case class OutPoint(transactionHash : Hash, outputIndex : Int) extends ProtocolMessage

/**
 *  Wallet Transaction Attribute ; Set to one of the following values:
 *  • send if sending payment
 *  • receive if this wallet received payment in a regular transaction
 *  • generate if a matured and spendable coinbase
 *  • immature if a coinbase that is not spendable yet
 *  • orphan if a coinbase from a block that’s not in the local best block chain
 *  • move if an off-block-chain move made with the move RPC
 */
object WalletTransactionAttribute extends Enumeration {
  type WalletTransactionAttribute = Value

/*
  val SEND     = new Val(nextId, "send")
  val RECEIVE  = new Val(nextId, "receive")
  val GENERATE = new Val(nextId, "generate")
  val IMMATURE = new Val(nextId, "immature")
  val ORPHAN   = new Val(nextId, "orphan")
  val MOVE     = new Val(nextId, "move")
*/

  val SEND     = Value
  val RECEIVE  = Value
  val GENERATE = Value
  val IMMATURE = Value
  val ORPHAN   = Value
  val MOVE     = Value

}

/** A transaction stored for an output ownership.
  */
case class WalletTransaction(
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
    time              : Long,                   // 1418695703,
    // A Unix epoch time when the transaction was detected by the local node,
    // or the time of the block on the local best block chain that included the transaction.
    // Not returned for move category payments
// P2
//    timereceived      : Option[Long],          // 1418925580

    // The transaction related with this wallet transaction.
    transaction : Transaction
) extends ProtocolMessage

case class OwnershipDescriptor(privateKeys : List[String]) extends ProtocolMessage

case class WalletOutput(
    // Some(block height) of the block on the local best block chain which includes this transaction. None otherwise.
    blockindex        : Option[Long],          // 11,
    // Whether this output is in the generation transaction.
    coinbase : Boolean,
    // Whether this coin was spent or not.
    spent : Boolean,
    // The transaction output
    transactionOutput : TransactionOutput
) extends ProtocolMessage
