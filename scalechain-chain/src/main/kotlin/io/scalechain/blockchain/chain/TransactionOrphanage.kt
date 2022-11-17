package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.OrphanTransactionDescriptor
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.BlockStorage
import io.scalechain.blockchain.storage.index.KeyValueDatabase

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionOrphanage(private val storage : BlockStorage) {
  /**
    * Remove a transaction from the indexes maintaining the orphans.
    *
    * @param orphanTxHash The hash of the accepted orphan transaction to remove.
    */
  fun delOrphan(db : KeyValueDatabase, orphanTxHash : Hash) : Unit {
    storage.delOrphanTransaction(db, orphanTxHash)
  }

  /**
    * Add an orphan transaction.
    *
    * @param txHash The hash of the orphan transaction
    * @param transaction The orphan transaction.
    */
  fun putOrphan(db : KeyValueDatabase, txHash : Hash, transaction : Transaction) : Unit {
    // TODO : BUGBUG : Need a recovery mechanism for the crash during the excution of this method.

    // Step 1 : Add the orphan transaction itself.
    storage.putOrphanTransaction(db, txHash, OrphanTransactionDescriptor(transaction))

    // Step 2 : Find all inputs that depend on a missing parent transaction.
    val missingTransactions = transaction.inputs.map{ it.outputTransactionHash }.filterNot { outputTxHash : Hash ->
      storage.hasTransaction(db, outputTxHash)
    }

    // Step 3 : Add the orphan transaction indexed by the missing parent transactions.
    missingTransactions.forEach { missingTxHash : Hash ->
      storage.addOrphanTransactionByParent(db, missingTxHash, txHash)
    }
  }


  /** Get the orphan transaction
    *
    * @param txHash The hash of the orphan transaction to get.
    * @return Some(transaction) if the orphan exists; None otherwise.
    */
  fun getOrphan(db : KeyValueDatabase, txHash : Hash) : Transaction? {
    return storage.getOrphanTransaction(db, txHash)?.transaction
  }

  /** Check if the orphan exists.
    *
    * @param txHash The hash of the orphan to check the existence.
    * @return true if it exists; false otherwise.
    */
  fun hasOrphan(db : KeyValueDatabase, txHash : Hash) : Boolean {
    // TODO : OPTIMIZE : Just check if the orphan exists without decoding the block data.
    return storage.getOrphanTransaction(db, txHash) != null
  }


  /** Get the list of orphan transaction hashes depending the given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  fun getOrphansDependingOn(db : KeyValueDatabase, blockHash : Hash) : List<Hash> {
    return storage.getOrphanTransactionsByParent(db, blockHash)
  }

  /** Remove the dependent transactions on a given hash.
    *
    * @param blockHash The mapping from the block hash to the hashes of transactions depending on it is removed.
    */
  fun removeDependenciesOn(db : KeyValueDatabase, blockHash : Hash) : Unit {
    storage.delOrphanTransactionsByParent(db, blockHash)
  }
}
