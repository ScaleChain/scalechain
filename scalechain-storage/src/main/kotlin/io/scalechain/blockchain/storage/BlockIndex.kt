package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.index.KeyValueDatabase

/**
  * Created by kangmo on 11/16/15.
  */
trait BlockIndex {
  /** Get a block by its hash.
    *
    * @param blockHash
    */
  def getBlock(blockHash : Hash)(implicit db : KeyValueDatabase) : Option[(BlockInfo, Block)]

  /** Get a transaction by its hash.
    *
    * @param transactionHash
    */
  def getTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Option[Transaction]
}