package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import org.slf4j.LoggerFactory

/** Processes a received transaction.
  *
  */
open class TransactionProcessor(val chain : Blockchain) {
  private val logger = LoggerFactory.getLogger(TransactionProcessor::class.java)

  /** See if a transaction exists. Checks orphan transactions as well.
    * naming rule : 'exists' checks orphan transactions as well, whereas hasNonOrphan does not.
    *
    * @param txHash The hash of the transaction to check the existence.
    * @return true if the transaction was found; None otherwise.
    */
  fun exists(db : KeyValueDatabase, txHash : Hash) : Boolean {
    return chain.hasTransaction(db, txHash) || chain.txOrphanage.hasOrphan(db, txHash)
  }

  /** Get a transaction either from a block or from the transaction disk-pool.
    * getTransaction does not return orphan transactions.
    *
    * @param txHash The hash of the transaction to get.
    * @return Some(transaction) if the transaction was found; None otherwise.
    */
  fun getTransaction(db : KeyValueDatabase, txHash : Hash) : Transaction? {
    return chain.getTransaction(db, txHash)
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
  fun putTransaction(db : KeyValueDatabase, txHash : Hash, transaction : Transaction) : Unit {
//    synchronized { // To prevent double spends, we need to synchronize transactions to put.
      // TODO : Need to check if the validity of the transation?
//      chain.withTransaction { implicit transactingDB =>
//        chain.putTransaction(txHash, transaction)(transactingDB)
//      }
//    }
    // TODO : BUGBUG : Change to record level locking with atomic update.
    chain.putTransaction(db, txHash, transaction)
  }

  /**
    * Recursively accepts children of the given parent.
    *
    * @param initialParentTxHash The hash of the parent transaction that an orphan might depend on.
    * @return The list of hashes of accepted children transactions.
    */
  fun acceptChildren(db : KeyValueDatabase, initialParentTxHash : Hash) : List<Hash> {
    synchronized(this) { // do not allow two threads run acceptChildren at the same time.
      val acceptedChildren = arrayListOf<Hash>()

      var i = -1;
      do {
        val parentTxHash = if (acceptedChildren.size == 0) initialParentTxHash else acceptedChildren[i]

        val dependentChildren : List<Hash> = chain.txOrphanage.getOrphansDependingOn(db, parentTxHash)

        //chain.withTransaction { transactionalDB =>
          dependentChildren.forEach { dependentChildHash : Hash ->
            val dependentChild = chain.txOrphanage.getOrphan(db, dependentChildHash)
            if (dependentChild != null) {
              try {
                //println(s"trying to accept a child. ${dependentChildHash}")
                // Try to add to the transaction pool.
                putTransaction(db, dependentChildHash, dependentChild)
                // add the hash to the acceptedChildren so that we can process children of the acceptedChildren as well.
                acceptedChildren.add(dependentChildHash)
                // del the orphan
                chain.txOrphanage.delOrphan(db, dependentChildHash)

                //println(s"accepted a child. ${dependentChildHash}")

              } catch(e : ChainException) {
                if (e.code == ErrorCode.TransactionOutputAlreadySpent) { // The orphan turned out to be a conflicting transaction.
                  // do nothing.
                  // TODO : Add a test case.
                } else if ( e.code == ErrorCode.ParentTransactionNotFound) { // The transaction depends on another parent transaction.
                  // do nothing. Still an orphan transaction.
                  // TODO : Add a test case.
                } else {
                  throw e
                }
              }
            } else {
              // The orphan tranasction was already deleted. nothing to do.
            }
          }

          chain.txOrphanage.removeDependenciesOn(db, parentTxHash)
        //}
        i += 1
      } while( i < acceptedChildren.size)

      // Remove duplicate by converting to a set, and return as a list.
      return acceptedChildren.toSet().toList()
    }
  }

  /**
    * Add an orphan transaction.
    *
    * @param txHash The hash of the orphan transaction
    * @param transaction The orphan transaction.
    */
  fun putOrphan(db : KeyValueDatabase, txHash : Hash, transaction : Transaction) : Unit {
    return chain.txOrphanage.putOrphan(db, txHash, transaction)
  }

  companion object : TransactionProcessor(Blockchain.get()) {
  }
}
