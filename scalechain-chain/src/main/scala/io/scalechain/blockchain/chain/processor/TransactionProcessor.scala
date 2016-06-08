package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{Transaction, Hash}

/** Processes a received transaction.
  *
  */
object TransactionProcessor {
  lazy val chain = Blockchain.get

  /** Get a transaction either from a block or from the transaction disk-pool.
    *
    * @param txHash The hash of the transaction to get.
    * @return Some(transaction) if the transaction was found; None otherwise.
    */
  def getTransaction(txHash : Hash) : Option[Transaction] = {
    chain.getTransaction(txHash)
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
