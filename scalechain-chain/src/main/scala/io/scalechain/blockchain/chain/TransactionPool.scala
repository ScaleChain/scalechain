package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{TransactionPoolEntry, TransactionDescriptor, Hash, Transaction}
import io.scalechain.blockchain.storage.BlockStorage
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionPool(storage : BlockStorage, txMagnet : TransactionMagnet) {
  private val logger = LoggerFactory.getLogger(classOf[TransactionPool])

  def getTransactionsFromPool() : List[(Hash, Transaction)] = {
    storage.getTransactionsFromPool().map{ case (hash, transactionPoolEntry) =>
      (hash, transactionPoolEntry.transaction)
    }
  }

  /**
    * Add a transaction to disk pool.
    *
    * Assumption : The transaction was pointing to a transaction record location, which points to a transaction written while the block was put into disk.
    * Caution : This method should be called with Blockchain.get.synchronized, because this method updates the spending in-points of transactions.
    *
    * @param txHash The hash of the transaction to add.
    * @param transaction The transaction to add to the disk-pool.
    *
    * @return true if the transaction was valid with all inputs connected. false otherwise. (ex> orphan transactions return false )
    */
  def addTransactionToPool(txHash : Hash, transaction : Transaction) : Unit = {
    // Step 01 : Check if the transaction exists in the disk-pool.
    if ( storage.getTransactionFromPool(txHash).isDefined ) {
      logger.info(s"A duplicate transaction in the pool was discarded. Hash : ${txHash}")
    } else {
      // Step 02 : Check if the transaction exists in a block in the best blockchain.
      val txDescOption = storage.getTransactionDescriptor(txHash)
      if (txDescOption.isDefined ) {
        logger.info(s"A duplicate transaction in on a block was discarded. Hash : ${txHash}")
      } else {
        // Step 03 : CheckTransaction - check values in the transaction.

        // Step 04 : IsCoinBase - the transaction should not be a coinbase transaction. No coinbase transaction is put into the disk-pool.

        // Step 05 : GetSerializeSize - Check the serialized size

        // Step 06 : GetSigOpCount - Check the script operation count.

        // Step 07 : IsStandard - Check if the transaction is a standard one.

        // Step 08 : Check the transaction fee.

        // Step 09 : Check for double-spends with existing transactions
        // First, check only without affecting the transaction database. If something is wrong such as double spending issues, an exception is raised.
        txMagnet.attachTransaction(txHash, transaction, None, checkOnly = true)

        // Step 09 : Add to the disk-pool
        txMagnet.attachTransaction(txHash, transaction, None, checkOnly = false)

        logger.info(s"A new transaction was put into pool. Hash : ${txHash}")
      }
    }
  }

  /**
    * Remove a transaction from the disk pool.
    * Called when a block is attached. We should not detach transaction inputs, because the inputs should still be attached.
    *
    * @param txHash The hash of the transaction to remove.
    */
  protected[chain] def removeTransactionFromPool(txHash : Hash) : Unit = {
    // Note : We should not touch the TransactionDescriptor.
    storage.delTransactionFromPool(txHash)
  }

}
