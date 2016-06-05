package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.blockchain.storage.TransactionLocator
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

object DatabaseTablePrefixes {
  val BLOCK_INFO : Byte = 'b'
  val TRANSACTION : Byte = 't'
  val BLOCK_FILE_INFO : Byte = 'f'
  val LAST_BLOCK_FILE : Byte = 'l'
  val BEST_BLOCK_HASH : Byte = 'B'
  val BLOCK_HEIGHT : Byte = 'h'

  val ORPHAN_BLOCK : Byte = '1'
  val ORPHAN_TRANSACTION : Byte = '2'
  val ORPHAN_BLOCKS_BY_PARENT : Byte = '3'
  val ORPHAN_TRANSACTIONS_BY_DEPENDENCY : Byte = '4'
}

/** Maintains block chains with different height, it knows which one is the best one.
  *
  * This class is used by CassandraBlockStorage.
  */
class BlockDatabase(db : KeyValueDatabase) {
  val logger = LoggerFactory.getLogger(classOf[BlockDatabase])

  import DatabaseTablePrefixes._

  def getBlockInfo(hash : Hash) : Option[BlockInfo] = {
    db.getObject(BLOCK_INFO, hash)(HashCodec, BlockInfoCodec)
  }

  /** Get the block hash at the given height on the best blockchain.
    *
    * @param height The height of the block.
    * @return The hash of the block at the height on the best blockchain.
    */
  def getBlockHashByHeight(height : Long) : Option[Hash] = {
    db.getObject(BLOCK_HEIGHT, BlockHeight(height))(BlockHeightCodec, HashCodec)
  }

  /** Put the block hash searchable by height.
    *
    * @param height The height of the block hash. The block should be on the best blockchain.
    * @param hash The hash of the block.
    */
  def putBlockHashByHeight(height : Long, hash : Hash) : Unit = {
    db.putObject(BLOCK_HEIGHT, BlockHeight(height), hash)(BlockHeightCodec, HashCodec)
  }

  /** Update the hash of the next block.
    *
    * @param hash The block to update the next block hash.
    * @param nextBlockHash Some(nextBlockHash) if the block is on the best blockchain, None otherwise.
    */
  def updateNextBlockHash(hash : Hash, nextBlockHash : Option[Hash]) = {
    val blockInfoOption : Option[BlockInfo] = getBlockInfo(hash)

    assert(blockInfoOption.isDefined)

    putBlockInfo(hash, blockInfoOption.get.copy(
      nextBlockHash = nextBlockHash
    ))
  }

  def getBlockHeight(hash : Hash) : Option[Int] = {
    getBlockInfo(hash).map(_.height)
  }

  def putBlockInfo(hash : Hash, info : BlockInfo) : Unit = {
    val blockInfoOption = getBlockInfo(hash)
    if (blockInfoOption.isDefined) {
      val currentBlockInfo = blockInfoOption.get
      // hit an assertion : put a block info with different height
      assert(currentBlockInfo.height == info.height)

      // hit an assertion : put a block info with a different block locator.
      if (info.blockLocatorOption.isDefined) {
        if ( currentBlockInfo.blockLocatorOption.isDefined ) {
          assert( currentBlockInfo.blockLocatorOption.get == info.blockLocatorOption.get )
        }
      }

      // hit an assertion : change any field on the block header
      assert(currentBlockInfo.blockHeader == info.blockHeader)
    }

    db.putObject(BLOCK_INFO, hash, info)(HashCodec, BlockInfoCodec)
  }

  def putBestBlockHash(hash : Hash) : Unit = {
    db.putObject(Array(BEST_BLOCK_HASH), hash)(HashCodec)
  }

  def getBestBlockHash() : Option[Hash] = {
    db.getObject(Array(BEST_BLOCK_HASH))(HashCodec)
  }

  def close() = db.close()
}

/** BlockDatabase for use with RecordStorage.
  *
  * Additional features : tracking block file info, transaction locators.
  *
  * When storing blocks with RecordStorage, we need to keep track of block file information.
  * We also should have a locator of each transactions keyed by the transaction hash.
  */
class BlockDatabaseForRecordStorage(db : KeyValueDatabase) extends BlockDatabase(db){
  import DatabaseTablePrefixes._

  /** Put transactions into the transaction index.
    * Key : transaction hash
    * Value : FileRecordLocator for the transaction.
    *
    * @param transactions
    * @return
    */
  def putTransactions(transactions : List[(Transaction, TransactionLocator)]) = {
    for ( (transaction, txLocatorDesc) <- transactions) {
      val txDesc =
        TransactionDescriptor(
          Left(txLocatorDesc.txLocator),
          List.fill(transaction.outputs.length)(None) )

      putTransactionDescriptor(txLocatorDesc.txHash, txDesc)

      assert( txLocatorDesc.txHash == transaction.hash )
    }
  }

  // TODO : Add test case
  def getTransactionDescriptor(txHash : Hash) : Option[TransactionDescriptor] = {
    db.getObject(TRANSACTION, txHash)(HashCodec, TransactionDescriptorCodec)
  }

  // TODO : Add test case
  def putTransactionDescriptor(txHash : Hash, transactionDescriptor : TransactionDescriptor) = {
    db.putObject(TRANSACTION, txHash, transactionDescriptor)(HashCodec, TransactionDescriptorCodec)
  }

  /** Remove a transaction from the transaction index.
    *
    * @param txHash The hash of the transaction to remove.
    */
  def delTransaction(txHash : Hash ) : Unit = {
    db.delObject(TRANSACTION, txHash)(HashCodec)
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
      assert( currentInfo.fileSize < blockFileInfo.fileSize )


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

}