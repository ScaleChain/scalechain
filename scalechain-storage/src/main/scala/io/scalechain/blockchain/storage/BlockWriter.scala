package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.codec.{TransactionCodec, TransactionCountCodec, BlockHeaderCodec, BlockCodec}
import io.scalechain.blockchain.proto.{TransactionCount, Hash, Block, FileRecordLocator}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.record.BlockRecordStorage


case class TransactionLocator(txHash : Hash, txLocator : FileRecordLocator)

case class AppendBlockResult(blockLocator : FileRecordLocator, headerLocator : FileRecordLocator, txLocators : List[TransactionLocator])

/** Write a block on the disk block storage.
  *
  * @param storage The block record storage where we write our block and transactions in it.
  */
class BlockWriter(storage : BlockRecordStorage) {
  /** Append a block to the given disk block storage,
    * producing file record locators for the block as well as each transaction in the block.
    *
    * Why? When we put a block into disk block storage, we have to create an index by block hash.
    * We also have to create an index by transaction hash that points to each transaction in the written block.
    *
    * This is necessary to read a specific transaction by hash, to get unspent output using an out point.
    * (An out point points to an output of a transaction using transaction hash and output index. )
    *
    * @param block The block to write.
    * @return AppendBlockResult, which is the header locator and transaction locators.
    */
  def appendBlock(block:Block): AppendBlockResult = {
    /**
      * To get a record locator of each transaction we write while we write a block,
      * We need to write (1) block header (2) transaction count (3) each transaction.
      *
      * This is why we need a separate case class for the transaction count.
      * If we write a block as a whole, we can't get record locators for each transaction in a block.
      */

    // Step 1 : Write block header
    val blockHeaderLocator = storage.appendRecord(block.header)(BlockHeaderCodec)

    // Step 2 : Write transaction count
    storage.appendRecord(TransactionCount(block.transactions.size))(TransactionCountCodec)

    // Step 3 : Write each transaction
    val txLocators =
      for( transaction <- block.transactions;
           txHash = Hash( HashCalculator.transactionHash(transaction) );
           txLocator = storage.appendRecord(transaction)(TransactionCodec)
      ) yield TransactionLocator(txHash, txLocator)

    // Step 4 : Calculate block locator
    // The AppendBlockResult.headerLocator has its size 80(the size of block header)
    // We need to use the last transaction's (offset + size), which is the block size to get the block locator.
    val lastTxLocator = txLocators.last.txLocator.recordLocator
    val blockSizeExceptTheLastTransaction = lastTxLocator.offset - blockHeaderLocator.recordLocator.offset

    // CRITICAL BUGBUG : If a new record storage file is added, the last transaction and the block header is written in different file.
    val blockSize = blockSizeExceptTheLastTransaction + lastTxLocator.size

    //println(s"blockHeaderLocator=${blockHeaderLocator}, txLocators.last.txLocator=${txLocators.last.txLocator}, blockSizeExceptTheLastTransaction=$blockSizeExceptTheLastTransaction, blockSize=$blockSize")

    val blockLocator = blockHeaderLocator.copy(
      recordLocator = blockHeaderLocator.recordLocator.copy (
        size = blockSize.toInt
      )
    )

    AppendBlockResult(blockLocator, blockHeaderLocator, txLocators)
  }
}
