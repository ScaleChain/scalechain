package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Block, FileRecordLocator, TransactionHash}


case class TransactionLocator(txHash : TransactionHash, txLocator : FileRecordLocator)

case class AppendBlockResult(blockLocator : FileRecordLocator, txLocators : List[TransactionLocator])

/** Write a block on the disk block storage.
  */
class BlockWriter {
  /** append a block to the given disk block storage,
    * producing file record locators for the block as well as each transaction in the block.
    *
    * Why? When we put a block into disk block storage, we have to create an index by block hash.
    * We also have to create an index by transaction hash that points to each transaction in the written block.
    *
    * This is necessary to read a specific transaction by hash, to get unspent output using an out point.
    * (An out point points to an output of a transaction using transaction hash and output index. )
    * @param storage
    * @param block
    * @return
    */
  def appendBlock(storage : DiskBlockStorage, block:Block): AppendBlockResult = {
    // TODO : Implement
    assert(false)
    null
  }
}
