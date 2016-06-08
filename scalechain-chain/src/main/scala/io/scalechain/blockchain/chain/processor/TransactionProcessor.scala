package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{Transaction, Hash}

/** Processes a received transaction.
  *
  */
object TransactionProcessor {
  val chain = Blockchain.get

  /** See if a transaction exists. Checks orphan transactions as well.
    *
    * @param txHash The hash of the transaction to check the existence.
    * @return true if the transaction was found; None otherwise.
    */
  def hasTransaction(txHash : Hash) : Boolean = {
    // TODO : Implement
    assert(false)
    false
  }

  /** Get a transaction either from a block or from the transaction disk-pool.
    * getTransaction does not return orphan transactions.
    *
    * @param txHash The hash of the transaction to get.
    * @return Some(transaction) if the transaction was found; None otherwise.
    */
  def getTransaction(txHash : Hash) : Option[Transaction] = {
    chain.getTransaction(txHash)
  }

  /**
    * Add a transaction to disk pool.
    *
    * Assumption : The transaction was pointing to a transaction record location, which points to a transaction written while the block was put into disk.
    *
    * @param txHash The hash of the transaction to add.
    * @param transaction The transaction to add to the disk-pool.
    * @return true if the transaction was valid with all inputs connected. false otherwise. (ex> orphan transactions return false )
    */
  def addTransactionToDiskPool(txHash : Hash, transaction : Transaction) : Unit = {
    chain.addTransactionToDiskPool(txHash, transaction)
  }

  /**
    * Recursively accepts children of the given parent.
    *
    * @param parentTxHash The hash of the parent transaction that an orphan might depend on.
    * @return The list of hashes of accepted children transactions.
    */
  def acceptChildren(parentTxHash : Hash) : List[Hash] = {
    // TODO : Implement
    assert(false)
    null
  }

  /**
    * Remove transactions from the indexes maintaining the orphans.
    *
    * @param orphanTransactions The list of hashes of the accepted orphan transactions to remove.
    */
  def removeOrphanTransactions(orphanTransactions : List[Hash]) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /**
    * Add an orphan transaction.
    *
    * @param txHash The hash of the orphan transaction
    * @param transaction The orphan transaction.
    */
  def addOrphanTransaction(txHash : Hash, transaction : Transaction) : Unit = {
    // TODO : Implement
    assert(false)

    // Step 1 : Add the orphan transaction itself.

    // Step 2 : Find all inputs that depend on a missing parent transaction.

    // Step 3 : Add the orphan transaction indexed by the missing parent transactions.

  }
}
