package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.TransactionCountCodec
import io.scalechain.blockchain.proto.codec.BlockHeaderCodec

import io.scalechain.blockchain.proto.TransactionCount
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.record.BlockRecordStorage


data class TransactionLocator(val txHash : Hash, val txLocator : FileRecordLocator)

data class AppendBlockResult(val blockLocator : FileRecordLocator, val headerLocator : FileRecordLocator, val txLocators : List<TransactionLocator>)

/** Write a block on the disk block storage.
  *
  * @param storage The block record storage where we write our block and transactions in it.
  */
class BlockWriter(private val storage : BlockRecordStorage) {
  /** Append a block to the given disk block storage,
    * producing file record locators for the block as well as each transaction in the block.
    *
    * Why? When we put a block into disk block storage, we have to create an index by block hash.
    * We also have to create an index by transaction hash that points to each transaction in the written block.
    *
    * This is necessary to read a specific transaction by hash, to get unspent output using an out point.
    * (An out point points to an output of a transaction using transaction hash and output index. )
    *
    * Note : When a new record file is created between appending a block header and appending transactions,
    *        we have to append block on the file again.
    *
    *        Otherwise, our logic to calculate the size of the block is too complicated,
    *        as we assume that the block header and transactions are written in the same record file.
    *
    * @param block The block to write.
    * @return AppendBlockResult, which is the header locator and transaction locators.
    */
  fun appendBlock(block:Block) :AppendBlockResult {
    try {
      return appendBlockInternal(block)
    } catch(e : RecordFileChangedWhileWritingBlock)  {
      // A new block was created while writing a block
      // Call appendBlockInternal again, to append the block on the new file.
      return appendBlockInternal(block)
    }
  }

  /** An internal version of the appendBlock. Throws an exception if a new record file was created between appending a block header and appending transactions.
    *
    * Need to keep appendBlockInternal consistent with BlockWriter.getTxLocators.
    * - Whenever the block format changes, we need to apply the change to both methods.
    *
    * @param block The block to append
    * @throws BlockWriter.RecordFileChangedWhileWritingBlock A new record file was created between appending a block header and appending transactions.
    *                                                        The caller should call this function again, to append the block on the new file.
    * @return The AppendBlockResult.
    */
  fun appendBlockInternal(block:Block): AppendBlockResult {
    /**
      * To get a record locator of each transaction we write while we write a block,
      * We need to write (1) block header (2) transaction count (3) each transaction.
      *
      * This is why we need a separate data class for the transaction count.
      * If we write a block as a whole, we can't get record locators for each transaction in a block.
      */

    // Step 1 : Write block header
    val blockHeaderLocator = storage.appendRecord(BlockHeaderCodec, block.header)

    // Step 2 : Write transaction count
    storage.appendRecord(TransactionCountCodec, TransactionCount(block.transactions.size.toLong()))

    // Step 3 : Write each transaction
    val txLocators = block.transactions.map { transaction ->
      val txLocator = storage.appendRecord(TransactionCodec, transaction)
      // Step 31 : Check if a file was created during step 2 or step 3.
      if (blockHeaderLocator.fileIndex != txLocator.fileIndex) {
        // BUGBUG : We have written a transaction on the file, and the space is wasted.
        throw RecordFileChangedWhileWritingBlock()
      }
      TransactionLocator(transaction.hash(), txLocator)
    }

    // Step 4 : Calculate block locator
    // The AppendBlockResult.headerLocator has its size 80(the size of block header)
    // We need to use the last transaction's (offset + size), which is the block size to get the block locator.
    val lastTxLocator = txLocators.last().txLocator.recordLocator
    val blockSizeExceptTheLastTransaction = lastTxLocator.offset - blockHeaderLocator.recordLocator.offset

    // CRITICAL BUGBUG : If a record storage file is added, the last transaction and the block header is written in different file.
    val blockSize = blockSizeExceptTheLastTransaction + lastTxLocator.size

    //println(s"blockHeaderLocator=${blockHeaderLocator}, txLocators.last.txLocator=${txLocators.last.txLocator}, blockSizeExceptTheLastTransaction=$blockSizeExceptTheLastTransaction, blockSize=$blockSize")

    val blockLocator = blockHeaderLocator.copy(
      recordLocator = blockHeaderLocator.recordLocator.copy (
        size = blockSize.toInt()
      )
    )

    return AppendBlockResult(blockLocator, blockHeaderLocator, txLocators)
  }

  companion object {
    private class RecordFileChangedWhileWritingBlock : Exception()

    /**
     *
     * Note :
     * - Need to keep appendBlockInternal consistent with BlockWriter.getTxLocators.
     * - Whenever the block format changes, we need to apply the change to both methods.
     *
     * @param blockLocator The locator of the block, where points to the on-disk location.
     * @param block The block data.
     * @return The list of transaction locators for each transaction in the block.
     */
    fun getTxLocators(blockLocator : FileRecordLocator, block : Block) : List<TransactionLocator> {
      // Step 1 : Calculate the size of block header
      val blockHeaderSize = BlockHeaderCodec.encode(block.header).size

      // Step 2 : Calculate the size of transaction count
      val transactionCountSize = TransactionCountCodec.encode(TransactionCount(block.transactions.size.toLong())).size

      // Step 3 : Calculate the transaction offset for the first transaction
      var transactionOffset = blockLocator.recordLocator.offset + blockHeaderSize + transactionCountSize

      // Step 4 : Calculate transaction locator of each transaction.
      val txLocators = block.transactions.map { transaction ->
        val transactionSize = TransactionCodec.encode(transaction).size
        val txLocator = blockLocator.copy(
            recordLocator = blockLocator.recordLocator.copy(
                offset = transactionOffset,
                size = transactionSize
            )
        )
        transactionOffset += transactionSize
        TransactionLocator(transaction.hash(), txLocator)
      }
      return txLocators
    }
  }
}

