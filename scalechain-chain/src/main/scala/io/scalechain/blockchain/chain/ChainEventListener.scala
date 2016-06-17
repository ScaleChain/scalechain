package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.transaction.ChainBlock

/**
  * A listener that subscribes chain events such as new transactions or removed transactions.
  */
trait ChainEventListener {
  /** Called whenever a new transaction comes into a block or the disk-pool.
    *
    * @param transaction The newly found transaction.
    */
  def onNewTransaction(transaction : Transaction)

  /** Called whenever a new transaction is removed from the disk-pool without being added to a block.
    * This also means the transaction does not exist in any block, as the disk-pool has transactions
    * that are not in any block in the best block chain.
    *
    * @param transaction The transaction removed from the disk-pool.
    */
  def onRemoveTransaction(transaction : Transaction)

  /** Invoked whenever a new block is attached to the best blockchain.
    *
    * @param chainBlock The block added to the best blockchain.
    */
  def onAttachBlock(chainBlock:ChainBlock) : Unit

  /** Invoked whenever a block is detached from the best blockchain during the block reorganization.
    *
    * @param chainBlock The block to remove from the best blockchain.
    */
  def onDetachBlock(chainBlock:ChainBlock) : Unit
}
