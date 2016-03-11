package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.db.KeyValueDatabase

/**
  * Created by kangmo on 3/11/16.
  */
class BlockDatabase(db : KeyValueDatabase) {

  def getBlockInfo(hash : Hash) : Option[BlockInfo] = {
    assert(false)
    None
  }

  def putBlockInfo(hash : Hash, info : BlockInfo) : Unit = {
    assert(false)
  }

  /** Put transactions into the transaction index.
    * Key : transaction hash
    * Value : FileRecordLocator for the transaction.
    *
    * @param blockLocator
    * @param transactions
    * @return
    */
  def putTransactions(blockLocator : FileRecordLocator, transactions : List[Transaction]) = {
    assert(false)
    null
  }

  def putBlockFileInfo(fileNumber : Int, blockFileInfo : BlockFileInfo) : Unit = {
    assert(false)
  }

  def getBlockFileInfo(fileNumber : Int) : Option[BlockFileInfo] = {
    assert(false)
    None
  }

  def putLastBlockFile(fileNumber : Int) : Unit = {
    assert(false);
  }

  def getLastBlockFile() : Option[Int] = {
    assert(false)
    None
  }

  def putBestBlockHash(hash : Hash) : Unit = {
    assert(false)
  }

  def getBestBlockHash() : Option[Hash] = {
    assert(false)
    None
  }
}
