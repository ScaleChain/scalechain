package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.ChainBlock

/**
  * A listener that subscribes chain events such as new transactions or removed transactions.
  */
trait ChainEventListener {
  /** Called whenever a new transaction comes into a block or the disk-pool.
    *
    * @param transaction The newly found transaction.
    */
  def onNewTransaction(transaction : Transaction, chainBlock : Option[ChainBlock], transactionIndex : Option[Int])(implicit db : KeyValueDatabase)

  /** Called whenever a new transaction is removed from the disk-pool without being added to a block.
    * This also means the transaction does not exist in any block, as the disk-pool has transactions
    * that are not in any block in the best block chain.
    *
    * @param transaction The transaction removed from the disk-pool.
    */
  def onRemoveTransaction(transaction : Transaction)(implicit db : KeyValueDatabase)
}
