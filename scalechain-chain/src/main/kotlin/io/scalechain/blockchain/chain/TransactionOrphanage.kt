package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.OrphanTransactionDescriptor
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.BlockStorage
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.RocksDatabase

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionOrphanage(storage : BlockStorage) {
  /**
    * Remove a transaction from the indexes maintaining the orphans.
    *
    * @param orphanTxHash The hash of the accepted orphan transaction to remove.
    */
  fun delOrphan(orphanTxHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    storage.delOrphanTransaction(orphanTxHash)
  }

  /**
    * Add an orphan transaction.
    *
    * @param txHash The hash of the orphan transaction
    * @param transaction The orphan transaction.
    */
  fun putOrphan(txHash : Hash, transaction : Transaction)(implicit db : KeyValueDatabase) : Unit {
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
  fun getOrphan(txHash : Hash)(implicit db : KeyValueDatabase) : Option<Transaction> {
    storage.getOrphanTransaction(txHash).map(_.transaction)
  }

  /** Check if the orphan exists.
    *
    * @param txHash The hash of the orphan to check the existence.
    * @return true if it exists; false otherwise.
    */
  fun hasOrphan(txHash : Hash)(implicit db : KeyValueDatabase) : Boolean {
    // TODO : OPTIMIZE : Just check if the orphan exists without decoding the block data.
    storage.getOrphanTransaction(txHash).isDefined
  }


  /** Get the list of orphan transaction hashes depending the given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  fun getOrphansDependingOn(blockHash : Hash)(implicit db : KeyValueDatabase) : List<Hash> {
    storage.getOrphanTransactionsByParent(blockHash)
  }

  /** Remove the dependent transactions on a given hash.
    *
    * @param blockHash The mapping from the block hash to the hashes of transactions depending on it is removed.
    */
  fun removeDependenciesOn(blockHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    storage.delOrphanTransactionsByParent(blockHash)
  }
}
