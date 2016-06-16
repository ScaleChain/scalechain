package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{OrphanTransactionDescriptor, Transaction, Hash}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.BlockStorage
import HashSupported._

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionOrphanage(storage : BlockStorage) {
  /**
    * Remove a transaction from the indexes maintaining the orphans.
    *
    * @param orphanTxHash The hash of the accepted orphan transaction to remove.
    */
  def delOrphan(orphanTxHash : Hash) : Unit = {
    storage.delOrphanTransaction(orphanTxHash)
  }

  /**
    * Add an orphan transaction.
    *
    * @param txHash The hash of the orphan transaction
    * @param transaction The orphan transaction.
    */
  def putOrphan(txHash : Hash, transaction : Transaction) : Unit = {
    // TODO : BUGBUG : Need a recovery mechanism for the crash during the excution of this method.

    // Step 1 : Add the orphan transaction itself.
    storage.putOrphanTransaction(txHash, OrphanTransactionDescriptor(transaction))

    // Step 2 : Find all inputs that depend on a missing parent transaction.
    val missingTransactions = transaction.inputs.map(_.outputTransactionHash).filterNot{ txHash : Hash =>
      storage.hasTransaction(txHash)
    }

    // Step 3 : Add the orphan transaction indexed by the missing parent transactions.
    missingTransactions foreach { missingTxHash : Hash =>
      storage.addOrphanTransactionByParent(missingTxHash, txHash)
    }
  }


  /** Get the orphan transaction
    *
    * @param txHash The hash of the orphan transaction to get.
    * @return Some(transaction) if the orphan exists; None otherwise.
    */
  def getOrphan(txHash : Hash) : Option[Transaction] = {
    storage.getOrphanTransaction(txHash).map(_.transaction)
  }

  /** Check if the orphan exists.
    *
    * @param txHash The hash of the orphan to check the existence.
    * @return true if it exists; false otherwise.
    */
  def hasOrphan(txHash : Hash) : Boolean = {
    // TODO : OPTIMIZE : Just check if the orphan exists without decoding the block data.
    storage.getOrphanTransaction(txHash).isDefined
  }


  /** Get the list of orphan transaction hashes depending the given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  def getOrphansDependingOn(blockHash : Hash) : List[Hash] = {
    storage.getOrphanTransactionsByParent(blockHash)
  }

  /** Remove the dependent transactions on a given hash.
    *
    * @param blockHash The mapping from the block hash to the hashes of transactions depending on it is removed.
    */
  def removeDependenciesOn(blockHash : Hash) : Unit = {
    storage.delOrphanTransactionsByParent(blockHash)
  }
}
