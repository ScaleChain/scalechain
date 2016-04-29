package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Transaction

/** Reorganizes blocks based on the chain work(the total number of hash calculations from the genesis block).
  * 
  */
class BlockReorganizer {

  /** Reorganize blocks when
    * This method is called when the new best block is not based on the original best block.
    *
    * @param originalBestBlock The original best block before the new best one was found.
    * @param newBestBlock The new best block, which has greater chain work than the original best block.
    */
  def reorganize(originalBestBlock : BlockDescriptor, newBestBlock : BlockDescriptor) : Unit = {
    assert( originalBestBlock.chainWork < newBestBlock.chainWork)
    // TODO : Implement
    assert(false)

    // Step 1 : find the common ancestor of the two blockchains.
    val commonBlock : BlockDescriptor = findCommonBlock(originalBestBlock, newBestBlock)

    // The transactions to add to the mempool. These are ones in the invalidated blocks but are not in the new blocks.
    val transactionsToAddToMempool : Seq[Transaction] = null

    // Step 2 : transactionsToAddToMempool: add all transactions in (commonBlock, originalBestBlock] to transactions.

    // Step 3 : transactionsToAddToMempool: remove all transactions in (commonBlock, newBestBlock]

    // Step 4 : move transactionsToAddToMempool to mempool.

    // Step 5 : update the best block in the storage layer.
  }

  /** Get the descriptor of the common ancestor of the two given blocks.
    *
    * @param block1 The first given block.
    * @param block2 The second given block.
    */
  def findCommonBlock(block1 : BlockDescriptor, block2 : BlockDescriptor) : BlockDescriptor = {
    // TODO : Implement
    assert(false)
    null
  }
}
