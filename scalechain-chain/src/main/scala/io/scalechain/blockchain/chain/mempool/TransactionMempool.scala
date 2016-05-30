package io.scalechain.blockchain.chain.mempool

import io.scalechain.blockchain.chain.OrphanTransactions
import io.scalechain.blockchain.proto.{Hash, Transaction}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.BlockStorage

/**
  * A meempool for keeping transactions which are not stored in blocks.
  *
  *  [ Data structure ]
  *
  *  1. Orphan pool : For the orphan transactions, which have at least one input that points to a missing transaction
  *  2. Normal pool : For transactions whose transactions pointed by inputs are not missing.
  *
  *  [ Operations ]
  *
  *  1. Add a transaction
  *    Add a transaction into the mempool. Check if it is an orphan. If yes, put it into the orphan pool.
  *    If no, put it into the normal pool. Recursively check if any orphan points to the new transaction
  *    to move the orphan to the normal pool.
  *
  *    <Called by>
  *    A. When a transaction was received by a peer.
  *    B. During block reorganization. transactions in the invalidated blocks are moved back to the mempool.
  *
  *  2. Remove a transaction.
  *    Remove a transaction from the mempool.
  *
  *    <Called by>
  *    A. When a new block was found in the longest blockchain. We need to remove all transactions
  *       included in the newly found block from the mempool.
  *
  *  3. Get a list of transactions to include into a block.
  *
  *    <Called by>
  *    A. When a miner tries to create a block, we have to create a block template first.
  *       The block template has the transactions to keep in the block.
  *
  * @param blockStorage The block storage that has all blocks and transactions in the blockchain.
  */
class TransactionMempool(blockStorage : BlockStorage) {

  /** For transactions whose transactions pointed by inputs are not missing.
    */
  val completeTransactions = new TransientTransactionStorage()

  /** For the orphan transactions, which have at least one input that points to a missing transaction.
    */
  val orphanPool = new OrphanTransactions()

  /** Put a transaction into the mempool.
    *
    * @param transaction The transaction to put into the mempool.
    */
  def put(transaction : Transaction): Unit = {
    // TODO : check if the transaction inputs are connected and points to unspent outputs.
    completeTransactions.put(transaction.hash, transaction)
  }

  /** Remove a transaction.
    *
    * @param txHash The hash of the transaction to remove.
    */
  def del(txHash : Hash) : Unit = {
    completeTransactions.del(txHash)
  }

  /** Check if the storage has a transaction.
    *
    * @param txHash The transaction hash to see if a transaction exists.
    * @return true if the transaction exists. false otherwise.
    */
  def exists(txHash : Hash): Boolean = {
    completeTransactions.exists(txHash)
  }

  /** Get a transaction by hash.
    *
    * @param txHash The transaction hash.
    * @return Some(transaction) if found; None otherwise.
    */
  def get(txHash : Hash) : Option[Transaction] = {
    completeTransactions.get(txHash)
  }

  /** Get transactions whose input transactions all exist.
    * This method is used to get the list of transactions to put into a newly created block.
    *
    * @return A sequence of transactions.
    */
  def getValidTransactions() : Iterator[Transaction] = {
    completeTransactions.transactions()
  }
}

