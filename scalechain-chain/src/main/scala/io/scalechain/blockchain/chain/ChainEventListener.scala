package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.transaction.ChainBlock

/**
  * A listener that subscribes chain events such as new transactions or removed transactions.
  */
trait ChainEventListener {
  /** Called whenever a new transaction comes into a block or the mempool.
    *
    * @param transaction The newly found transaction.
    */
  def onNewTransaction(transaction : Transaction)

  /** Called whenever a new transaction is removed from the mempool.
    * This also means the transaction does not exist in any block, as the mempool has transactions
    * that are not in any block in the best block chain.
    *
    * @param transaction The transaction removed from the mempool.
    */
  def onRemoveTransaction(transaction : Transaction)

  /** Invoked whenever a new block is added to the best blockchain.
    *
    * @param chainBlock The block added to the best blockchain.
    */
  def onNewBlock(chainBlock:ChainBlock) : Unit

  /** Invoked whenever a block is removed from the best blockchain during the block reorganization.
    *
    * @param chainBlock The block to remove from the best blockchain.
    */
  def onRemoveBlock(chainBlock:ChainBlock) : Unit
}
