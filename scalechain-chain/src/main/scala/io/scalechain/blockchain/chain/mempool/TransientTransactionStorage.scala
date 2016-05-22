package io.scalechain.blockchain.chain.mempool

import io.scalechain.blockchain.proto.{Transaction, Hash}
import io.scalechain.blockchain.script.HashCalculator

import scala.collection.mutable

/** Store/Retrieve transactions on a memory pool.
  */
class TransientTransactionStorage {
  /** The map from the transaction hash to a transaction.
    */
  val transactionsByHash = mutable.HashMap[Hash, Transaction]()

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
    transactionsByHash.contains(txHash)
  }

  /** Get the list of transactions in the transient storage /
    *
    * @return The list of transactions.
    */
  def transactions() : Iterator[Transaction] = {
    transactionsByHash.iterator.map{ case (hash, transaction ) =>
      transaction
    }
  }

}
