package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Transaction, TransactionHash, BlockHash, Block}

/** A block index enables searching a block by block hash, or searching a transaction by transaction hash.
 */
trait BlockIndex {
  /** Get a block by its hash.
   *
   * @param blockHash The hash of the block header to search.
   */
  def getBlock(blockHash : BlockHash) : Option[Block]

  /** Get a transaction by its hash.
   *
   * @param transactionHash The hash of the transaction to search.
   * @return The searched transaction.
   */
  def getTransaction(transactionHash : TransactionHash) : Option[Transaction]
}
