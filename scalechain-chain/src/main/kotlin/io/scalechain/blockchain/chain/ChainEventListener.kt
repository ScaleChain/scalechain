package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.ChainBlock

/**
  * A listener that subscribes chain events such as transactions or removed transactions.
  */
interface ChainEventListener {
  /** Called whenever a transaction comes into a block or the disk-pool.
    *
    * @param transaction The newly found transaction.
    */
  fun onNewTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction, chainBlock : ChainBlock?, transactionIndex : Int?)

  /** Called whenever a transaction is removed from the disk-pool without being added to a block.
    * This also means the transaction does not exist in any block, as the disk-pool has transactions
    * that are not in any block in the best block chain.
    *
    * @param transaction The transaction removed from the disk-pool.
    */
  fun onRemoveTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction)
}
