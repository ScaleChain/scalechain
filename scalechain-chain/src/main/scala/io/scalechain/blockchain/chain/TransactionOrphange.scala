package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{Transaction, Hash}
import io.scalechain.blockchain.storage.BlockStorage

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionOrphange(storage : BlockStorage) {
  /**
    * Remove transactions from the indexes maintaining the orphans.
    *
    * @param orphanTransactions The list of hashes of the accepted orphan transactions to remove.
    */
  def delOrphans(orphanTransactions : List[Hash]) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /**
    * Add an orphan transaction.
    *
    * @param txHash The hash of the orphan transaction
    * @param transaction The orphan transaction.
    */
  def putOrphan(txHash : Hash, transaction : Transaction) : Unit = {
    // TODO : Implement
    assert(false)

    // Step 1 : Add the orphan transaction itself.

    // Step 2 : Find all inputs that depend on a missing parent transaction.

    // Step 3 : Add the orphan transaction indexed by the missing parent transactions.

  }


  /** Get the orphan transaction
    *
    * @param txHash The hash of the orphan transaction to get.
    * @return Some(transaction) if the orphan exists; None otherwise.
    */
  def getOrphan(txHash : Hash) : Option[Transaction] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Check if the orphan exists.
    *
    * @param txHash The hash of the orphan to check the existence.
    * @return true if it exists; false otherwise.
    */
  def hasOrphan(txHash : Hash) : Boolean = {
    // TODO : Implement
    assert(false)
    false
  }


  /** Get the list of orphan transaction hashes depending the given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  def getOrphansDependingOn(blockHash : Hash) : List[Hash] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Remove the dependent transactions on a given hash.
    *
    * @param blockHash The mapping from the block hash to the hashes of transactions depending on it is removed.
    */
  def removeDependenciesOn(blockHash : Hash) : Unit = {
    // TODO : Implement
    assert(false)
  }
}
