package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto._

/** A block in a best blockchain.
  * Has all data of a block and also additional information such as the height of a block.
  *
  * @param height The height of the block in the best blockchain.
  * @param block The block itself.
  */
case class ChainBlock (
                        height : Long,
                        block : Block
                      )

/** The read-only view of the coins in the best blockchain.
  *
  */
trait CoinsView {
  /** Return a transaction output specified by a give out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  def getTransactionOutput(outPoint : OutPoint) : TransactionOutput
}

/** The read-only view of the best blockchain.
  */
trait BlockchainView extends CoinsView {
  /** Return an iterator that iterates each ChainBlock.
    *
    * Used by importaddress RPC to rescan blockchain to put related transactions and transaction outputs into the wallet database.
    *
    * @param height Specifies where we start the iteration. The height 0 means the genesis block.
    * @return The iterator that iterates each ChainBlock.
    */
  def getIterator(height : Long) : Iterator[ChainBlock]

  /** Return the block height of the best block.
    *
    * Used by RPCs to get the number of confirmations since a specific block.
    *
    * @return The best block height.
    */
  def getBestBlockHeight() : Long

  /** Return a transaction that matches the given transaction hash.
    *
    * Used by listtransaction RPC to get the
    *
    * @param transactionHash The transaction hash to search.
    * @return Some(transaction) if the transaction that matches the hash was found. None otherwise.
    */
  def getTransaction(transactionHash : Hash) : Option[Transaction]

}

