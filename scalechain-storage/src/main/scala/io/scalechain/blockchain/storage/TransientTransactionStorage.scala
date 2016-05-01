package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Hash, TransactionHash, Transaction}
import io.scalechain.blockchain.script.HashCalculator

import scala.collection.mutable

/** Store/Retrieve transactions on a memory pool.
  */
class TransientTransactionStorage {
  /** The map from the transaction hash to a transaction.
    */
  val transactionsByHash = mutable.HashMap[Hash, Transaction]()

  def put(transaction : Transaction): Unit = {
    val txHash = Hash( HashCalculator.transactionHash(transaction) )
    transactionsByHash.put(txHash, transaction)
  }

  /** Store a transaction on the storage.
    *
    * @param txHash The transaction hash.
    * @param transaction The transaction to store.
    */
  def put(txHash : Hash, transaction : Transaction): Unit = {
    transactionsByHash.put(txHash, transaction)
  }

  /** Search a transaction by the transaction hash.
    *
    * @param txHash The transaction hash.
    * @return The found transaction
    */
  def get(txHash : Hash ) : Option[Transaction] = {
    transactionsByHash.get(txHash)
  }

  /** Remove a transaction.
    *
    * @param txHash The hash of the transaction to remove.
    */
  def del(txHash : Hash) : Unit = {
    transactionsByHash.remove(txHash)
  }

  /** Check if the storage has a transaction.
    *
    * @param txHash The transaction hash to see if a transaction exists.
    * @return true if the transaction exists. false otherwise.
    */
  def exists(txHash : Hash): Boolean = {
    transactionsByHash.get(txHash).isDefined
  }

  /** Get transactions whose input transactions all exist and signatures are valid.
    * This method is used to get the list of transactions to put into a newly created block.
    *
    * @return A sequence of transactions.
    */
  def getValidTransactions() : Seq[Transaction] = {
    // TODO : Implement
    assert(false)
    null
  }
}
