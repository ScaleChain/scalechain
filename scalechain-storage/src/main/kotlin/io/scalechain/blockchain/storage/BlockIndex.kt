package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase

/**
  * Created by kangmo on 11/16/15.
  */
interface BlockIndex {
  /** Get a block by its hash.
    *
    * @param blockHash
    */
  fun getBlock(db : KeyValueDatabase, blockHash : Hash) : Pair<BlockInfo, Block>?

  /** Get a transaction by its hash.
    *
    * @param transactionHash
    */
  fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction?
}