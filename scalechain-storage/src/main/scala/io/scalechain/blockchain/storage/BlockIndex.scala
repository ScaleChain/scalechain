package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._

/**
  * Created by kangmo on 11/16/15.
  */
trait BlockIndex {
  /** Get a block by its hash.
    *
    * @param blockHash
    */
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)]

  /** Get a transaction by its hash.
    *
    * @param transactionHash
    */
  def getTransaction(transactionHash : Hash) : Option[Transaction]
}