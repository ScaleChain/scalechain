package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Hash, TransactionHash, Transaction}
import io.scalechain.blockchain.script.HashCalculator

import scala.collection.mutable

/** Store/Retrieve transactions on a memory pool.
  */
class TransientTransactionStorage extends TransactionStorage {
  /** The map from the transaction hash to a transaction.
    */
  val transactionsByHash = mutable.HashMap[Hash, Transaction]()

  /** Store a transaction on the storage.
    *
    * @param transaction The transaction to store.
    */
  def storeTransaction(transaction : Transaction): Unit = {
    val hash = Hash( HashCalculator.transactionHash(transaction) )
    transactionsByHash.put(hash, transaction)
  }

  /** Search a transaction by the transaction hash.
    *
    * @param hash The transaction hash.
    * @return The found transaction
    */
  def getTransaction(hash : Hash ) : Option[Transaction] = {
    transactionsByHash.get(hash)
  }

  /** Remove a transaction.
    *
    * @param hash The hash of the transaction to remove.
    */
  def removeTransaction(hash : Hash) : Unit = {
    transactionsByHash.remove(hash)
  }

  /** Check if the storage has a transaction.
    *
    * @param hash The transaction hash to see if a transaction exists.
    * @return true if the transaction exists. false otherwise.
    */
  def hasTransaction(hash : Hash): Boolean = {
    transactionsByHash.get(hash).isDefined
  }
}
