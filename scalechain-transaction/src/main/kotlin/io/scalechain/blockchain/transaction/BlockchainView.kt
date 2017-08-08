package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.ClosableIterator
import io.scalechain.blockchain.storage.index.KeyValueDatabase

/** A block in a best blockchain.
  * Has all data of a block and also additional information such as the height of a block.
  *
  * @param height The height of the block in the best blockchain.
  * @param block The block itself.
  */
data class ChainBlock (
                        val height : Long,
                        val block : Block
                      )

/** The read-only view of the coins in the best blockchain.
  *
  */
interface CoinsView {
  /** Return a transaction output specified by a give out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  fun getTransactionOutput(db : KeyValueDatabase, outPoint : OutPoint) : TransactionOutput
}

/** The read-only view of the best blockchain.
  */
interface BlockchainView : CoinsView {
  /** Return an iterator that iterates each ChainBlock.
    *
    * Used by importaddress RPC to rescan blockchain to put related transactions and transaction outputs into the wallet database.
    *
    * @param height Specifies where we start the iteration. The height 0 means the genesis block.
    * @return The iterator that iterates each ChainBlock.
    */
  fun getIterator(db : KeyValueDatabase, height : Long) : Iterator<ChainBlock>

  /** Return the block height of the best block.
    *
    * Used by RPCs to get the number of confirmations since a specific block.
    *
    * @return The best block height.
    */
  fun getBestBlockHeight() : Long

  /** Return a transaction that matches the given transaction hash.
    *
    * Used by listtransaction RPC
    *
    * @param transactionHash The transaction hash to search.
    * @return Some(transaction) if the transaction that matches the hash was found. None otherwise.
    */
  fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction?

}

