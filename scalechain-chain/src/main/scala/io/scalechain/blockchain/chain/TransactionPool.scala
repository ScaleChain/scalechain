package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{TransactionDescriptor, Hash, Transaction}
import io.scalechain.blockchain.storage.BlockStorage
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionPool(storage : BlockStorage, txMagnet : TransactionMagnet) {
  private val logger = LoggerFactory.getLogger(classOf[TransactionPool])

  def getTransactionsFromPool() : List[(Hash, Transaction)] = {
    storage.getTransactionsFromPool()
  }

  /**
    * Add a transaction to disk pool.
    *
    * Assumption : The transaction was pointing to a transaction record location, which points to a transaction written while the block was put into disk.
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
      if (txDescOption.isDefined && txDescOption.get.transactionLocatorOption.isDefined ) {
        logger.info(s"A duplicate transaction in on a block was discarded. Hash : ${txHash}")
      } else {
        // Step 03 : CheckTransaction - check values in the transaction.

        // Step 04 : IsCoinBase - the transaction should not be a coinbase transaction. No coinbase transaction is put into the disk-pool.

        // Step 05 : GetSerializeSize - Check the serialized size

        // Step 06 : GetSigOpCount - Check the script operation count.

        // Step 07 : IsStandard - Check if the transaction is a standard one.

        // Step 08 : Check for double-spends with existing transactions,
        txMagnet.attachTransactionInputs(txHash, transaction)

        // Step 09 : Check the transaction fee.

        // Step 10 : Add to the disk-pool
        storage.putTransactionToPool(txHash, transaction)

        // Step 11 : Add the transaction descriptor without the transactionLocatorOption.
        //          The transaction descriptor is necessary to mark the outputs of the transaction either spent or unspent.
        val txDesc =
          TransactionDescriptor(
            transactionLocatorOption = None,
            List.fill(transaction.outputs.length)(None) )

        storage.putTransactionDescriptor(txHash, txDesc)

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
