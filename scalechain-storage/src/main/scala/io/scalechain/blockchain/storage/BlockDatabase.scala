package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.blockchain.storage.db.KeyValueDatabase

object BlockDatabase {
  val BLOCK_INFO : Byte = 'b'
  val TRANSACTION : Byte = 't'
  val BLOCK_FILE_INFO : Byte = 'f'
  val LAST_BLOCK_FILE : Byte = 'l'
  val BEST_BLOCK_HASH : Byte = 'b'
}

/**
  * Created by kangmo on 3/11/16.
  */
class BlockDatabase(db : KeyValueDatabase) {
  import BlockDatabase._

  def getBlockInfo(hash : Hash) : Option[BlockInfo] = {
    db.getObject(BLOCK_INFO, hash)(HashCodec, BlockInfoCodec)
  }

  def putBlockInfo(hash : Hash, info : BlockInfo) : Unit = {
    db.putObject(BLOCK_INFO, hash, info)(HashCodec, BlockInfoCodec)
  }

  /** Put transactions into the transaction index.
    * Key : transaction hash
    * Value : FileRecordLocator for the transaction.
    *
    * @param transactions
    * @return
    */
  def putTransactions(transactions : List[(Hash, FileRecordLocator)]) = {
    for ( tx <- transactions) {
      val txHash  = tx._1 // transaction hash
      val locator = tx._2 // file record locator
      db.putObject(TRANSACTION, txHash, locator)(HashCodec, FileRecordLocatorCodec)
    }
  }

  def getTransactionLocator(txHash : Hash) : Option[FileRecordLocator] = {
    db.getObject(TRANSACTION, txHash)(HashCodec, FileRecordLocatorCodec)
  }

  def putBlockFileInfo(fileNumber : FileNumber, blockFileInfo : BlockFileInfo) : Unit = {
    // Input validation for the block file info.
    val currentInfoOption = getBlockFileInfo(fileNumber)
    if (currentInfoOption.isDefined) {
      val currentInfo = currentInfoOption.get
      // Can't put the same block info twice.
      assert( currentInfo != blockFileInfo )

      // First block height can't be changed.
      assert( currentInfo.firstBlockHeight == blockFileInfo.firstBlockHeight)

      // First block timestamp can't be changed.
      assert( currentInfo.firstBlockTimestamp == blockFileInfo.firstBlockTimestamp)

      // Block count should not be decreased.
      // Block count should increase
      assert( currentInfo.blockCount < blockFileInfo.blockCount)

      // File size should not be decreased
      // File size should increase
      assert( currentInfo.fileSize < blockFileInfo.fileSize)

      // The last block height should not be decreased.
      // The last block height should increase
      assert( currentInfo.lastBlockHeight < blockFileInfo.lastBlockHeight)

      // Caution : The last block timestamp can decrease.
    }

    db.putObject(BLOCK_FILE_INFO, fileNumber, blockFileInfo)(FileNumberCodec, BlockFileInfoCodec)
  }

  def getBlockFileInfo(fileNumber : FileNumber) : Option[BlockFileInfo] = {
    db.getObject(BLOCK_FILE_INFO, fileNumber)(FileNumberCodec, BlockFileInfoCodec)
  }

  def putLastBlockFile(fileNumber : FileNumber) : Unit = {
    // Input validation check for the fileNumber.
    val fileNumberOption = getLastBlockFile()
    if (fileNumberOption.isDefined) {
      // The file number should increase.
      assert( fileNumberOption.get.fileNumber < fileNumber.fileNumber )
    }

    db.putObject(Array(LAST_BLOCK_FILE), fileNumber)(FileNumberCodec)
  }

  def getLastBlockFile() : Option[FileNumber] = {
    db.getObject(Array(LAST_BLOCK_FILE))(FileNumberCodec)
  }

  def putBestBlockHash(hash : Hash) : Unit = {
    db.putObject(Array(BEST_BLOCK_HASH), hash)(HashCodec)
  }

  def getBestBlockHash() : Option[Hash] = {
    db.getObject(Array(BEST_BLOCK_HASH))(HashCodec)
  }

  def close() = db.close()
}
