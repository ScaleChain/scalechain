package io.scalechain.blockchain.proto

import io.scalechain.blockchain.proto.WalletTransactionAttribute.WalletTransactionAttribute

/** case classes that are used for keys or values of the wallet database.
  */

/** An outpoint points to an output in a transaction.
  *
  * @param transactionHash The hash of the transaction that has the output.
  * @param outputIndex The index of the output. The index starts from 0. Ex> The first output of a transaction has index 0.
  */
case class OutPoint(transactionHash : Hash, outputIndex : Int) extends ProtocolMessage

object WalletTransactionAttribute extends Enumeration {
  type WalletTransactionAttribute = Value
  val RECEIVING, SPENDING = Value
}

/** A transaction stored for an output ownership.
  *
  * @param outputOwnership The ownership that is either receiving UTXOs or spending UTXOs in a transaction.
  * @param attributes Whether the transaction is receiving UTXOs or spending UTXOs or both.
  * @param transactionHash Hash of the transaction related to the output ownership.
  */
case class WalletTransaction(outputOwnership : String, attributes: List[WalletTransactionAttribute], transactionHash : Hash) extends ProtocolMessage


case class UnspentTranasctionOutput(
 // The TXID of the transaction the output appeared in. The TXID must be encoded in hex in RPC byte order
 txid      : Hash,
 // The index number of the output (vout) as it appeared in its transaction, with the first output being 0
 vout      : Int,
 // The output’s pubkey script encoded as hex
 scriptPubKey : String,
 // If the pubkey script was a script hash, this must be the corresponding redeem script
 redeemScript : Option[String]
)
