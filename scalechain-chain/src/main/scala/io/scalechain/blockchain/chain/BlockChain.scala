package io.scalechain.blockchain.chain

import io.scalechain.blockchain.chain.mining.BlockTemplate
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.{DiskBlockStorage, GenesisBlock}

import io.scalechain.blockchain.chain.mempool.{TransactionMempool, TransientTransactionStorage}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Maintains the best blockchain, whose chain work is the biggest one.
  *
  * The blocks are kept in a tree data structure in memory.
  * The actual block data is not kept in memory, but the link from the child block to a parent block, as well as
  * summarizing information such as block hash and transaction count are kept.
  *
  * [ Overview ]
  *
  * The chain work for a block is the total number of hash calculations from block 0 to the current best block.
  *
  * For example, if we calculated hashes 10, 20, 15 times for three blocks B0, B1, and B2, the chain work is 45(10+20+15).
  *
  *   B0(10) → B1(10+20) → B2(10+20+15) : The best chain.
  *
  * Based on the total chain work of the new block, we decide the best blockchain.
  * For example, if we found a block B2' whose chain work(50) is greater than the current maxium(45),
  * we will keep B2' as the best block and update the best blockchain.
  *
  *   B0(10) → B1(10+20) → B2'(10+20+20) : The best chain.
  *                      ↘ B2(10+20+15) : This is a fork.
  *
  * When a new block B3 is added to the blockchain, we will add it on top of the best blockchain.
  *
  *   B0 → B1 → B2' → B3 : The best chain.
  *           ↘ B2
  *
  * Only transactions in the best blockchain remain effective.
  * Because B2 remains in a fork, all transactions in B2 are migrated to the mempool, except ones that are included in B3.
  *
  * The mempool is where transactions that are not in any block of the best blockchain are stored.
  *
  * Of course, block a reorganization can invalidate more than two blocks at once.
  *
  * Time T :
  *   B0(10) → B1(30) → B2(45) : The best chain.
  *
  * Time T+1 : Add B1' (chain work = 35)
  *   B0(10) → B1(30) → B2(45) : The best chain.
  *          ↘ B1'(35)
  *
  * Time T+2 : Add B2' (chain work = 55)
  *   B0(10) → B1(30) → B2(45)
  *          ↘ B1'(35) -> B2'(55) : The best chain.
  *
  * In this case all transactions in B1, B2 but not in B1' and B2' are moved to the mempool so that they can be added to
  * the block chain later when a new block is created.
  *
  */
class Blockchain {

  /** The descriptor of the best block.
    * This value is updated whenever a new best block is found.
    * We also have to check if we need to do block reorganization whenever this field is updated.
    */
  var theBestBlock : BlockDescriptor = null

  /** The blocks whose parents were not received yet.
    */
  val orphanBlocks = new OrphanBlocks()

  /** The memory pool for transient transactions that are valid but not stored in any block.
    */
  val mempool = new TransactionMempool(DiskBlockStorage.get())

  /** The in-memory index where we search blocks and transactions.
    */
  val chainIndex = new BlockchainIndex()

  /** Put a block onto the blockchain.
    *
    * (1) During initialization, we call putBlock for each block we received until now.
    * (2) During IBD(Initial Block Download), we call putBlock for blocks we downloaded.
    * (3) When a new block was received from a peer.
    *
    * Caller of this method should check if the bestBlock was changed.
    * If changed, we need to update the best block on the storage layer.
    *
    * @param block The block to put into the blockchain.
    */
  def putBlock(blockHash : BlockHash, block:Block) : Unit = {
    // Step 0 : Check if the block is the genesis block
    if (block.header.hashPrevBlock.isAllZero()) {
      assert(theBestBlock == null)
      theBestBlock = BlockDescriptor.create(null, blockHash, block)
    } else {
      assert(theBestBlock != null)

      // Step 1 : Get the block descriptor of the previous block.
      val prevBlockDesc: Option[BlockDescriptor] = chainIndex.findBlock(block.header.hashPrevBlock)

      // Step 2 : Create a new block descriptor for the given block.
      if (prevBlockDesc.isEmpty) {
        // The parent block was not received yet. Put it into an orphan block list.
        orphanBlocks.putBlock(blockHash, block)

        // BUGBUG : An attacker can fill up my memory with lots of orphan blocks.
      } else {
        val blockDesc = BlockDescriptor.create(prevBlockDesc.get, blockHash, block)

        // Step 3 : Check if the previous block of the new block is the current best block.
        if (prevBlockDesc == theBestBlock) {
          // Step 3.A.1 : Update the best block
          theBestBlock = blockDesc
        } else {
          // Step 3.B.1 : See if the chain work of the new block is greater than the best one.
          if (blockDesc.chainWork > theBestBlock.chainWork) {
            // Step 3.B.2 : Reorganize the blocks.
            reorganize(originalBestBlock = theBestBlock, newBestBlock = blockDesc)

            // Step 3.B.3 : Update the best block
            theBestBlock = blockDesc
          }
        }

        // Step 4 : Remove transactions in the block from the mempool.
        block.transactions.foreach { transaction =>
          val transactionHash = Hash( HashCalculator.transactionHash(transaction) )
          mempool.del(transactionHash)
        }
      }

      // Step 5 : Check if the new block is a parent of an orphan block.
      val orphans = orphanBlocks.findBlocks(blockHash)
      orphans.map { blocks =>
        orphanBlocks.removeBlocks(blockHash)
        blocks.getBlockList foreach { orphanBlock =>
          val blockHash = BlockHash(HashCalculator.blockHeaderHash(block.header))

          // Step 6 : Recursively call putBlock to put the orphans
          putBlock(blockHash, orphanBlock)
        }
      }
    }
  }

  /** Put a transaction we received from peers into the mempool.
    *
    * @param transaction The transaction to put into the mempool.
    */
  def putTransaction(transaction : Transaction) : Unit = {
    mempool.put(transaction)
  }




  /** Calculate the (encoded) difficulty bits that should be in the block header.
    *
    * @param prevBlockDesc The descriptor of the previous block. This method calculates the difficulty of the next block of the previous block.
    * @return
    */
  def calculateDifficulty(prevBlockDesc : BlockDescriptor) : Long = {
    if (prevBlockDesc.height == 0) { // The genesis block
      GenesisBlock.BLOCK.header.target
    } else {
      // BUGBUG : Make sure that the difficulty calculation is same to the one in the Bitcoin reference implementation.
      val currentBlockHeight = prevBlockDesc.height + 1
      if (currentBlockHeight % 2016 == 0) {
        // TODO : Calculate the new difficulty bits.
        assert(false)
        -1L
      } else {
        prevBlockDesc.blockHeader.target
      }
    }
  }

  /** Get the template for creating a block containing a list of transactions.
    *
    * @return The block template which has a sorted list of transactions to include into a block.
    */
  def getBlockTemplate() : BlockTemplate = {
    val difficultyBits = calculateDifficulty(prevBlockDesc = theBestBlock)

    val validTransactions = mempool.getValidTransactions()
    // Select transactions by priority and fee. Also, sort them.
    val sortedTransactions = selectTransactions(validTransactions, Block.MAX_SIZE)
    new BlockTemplate(difficultyBits, sortedTransactions)
  }


  /** Select transactions to include into a block.
    *
    *  Order transactions by fee in descending order.
    *  List N transactions based on the priority and fee so that the serialzied size of block
    *  does not exceed the max size. (ex> 1MB)
    *
    *  <Called by>
    *  When a miner tries to create a block, we have to create a block template first.
    *  The block template has the transactions to keep in the block.
    *  In the block template, it has all fields set except the nonce and the timestamp.
    *
    * @param transactions The candidate transactions
    * @param maxBlockSize The maximum block size. The serialized block size including the block header and transactions should not exceed the size.
    * @return The transactions to put into a block.
    */
  protected def selectTransactions(transactions : Seq[Transaction], maxBlockSize : Int) : List[Transaction] = {
    // TODO : Implement
    assert(false)

    // Step 1 : Select high priority transactions

    // Step 2 : Sort transactions by fee in descending order.

    // Step 3 : Choose transactions until we fill up the max block size.

    // Step 4 : Sort transactions based on a criteria to store into a block.
    // TODO : Need to check the sort order of transactions in a block.
    null
  }

  /** Reorganize blocks when
    * This method is called when the new best block is not based on the original best block.
    *
    * @param originalBestBlock The original best block before the new best one was found.
    * @param newBestBlock The new best block, which has greater chain work than the original best block.
    */
  protected def reorganize(originalBestBlock : BlockDescriptor, newBestBlock : BlockDescriptor) : Unit = {
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
  protected def findCommonBlock(block1 : BlockDescriptor, block2 : BlockDescriptor) : BlockDescriptor = {
    // TODO : Implement
    assert(false)
    null
  }
}

/** A block descriptor which has an fields to keep block chain metadata. The descriptor is kept in memory.
  */
object BlockDescriptor {
  /** Calculate the estimated number of hash calculations for a block.
    *
    * @param blockHash The block header hash to calculate the estimated number of hash calculations for creating the block.
    * @return The estimated number of hash calculations for the given block.
    */
  protected def getHashCalculations(blockHash : BlockHash) : Long = {
    // TODO : Implement
    assert(false)
    // Step 1 : Calculate the block hash

    // Step 2 : Calculate the (estimated) number of hash calculations based on the hash value.
    0
  }

  /** Create a block descriptor.
    *
    * @param previousBlock The block descriptor of the previous block.
    * @param blockHash The hash of the current block.
    * @param block The current block.
    * @return The created block descriptor.
    */
  def create(previousBlock : BlockDescriptor, blockHash : BlockHash, block : Block) : BlockDescriptor = {
    BlockDescriptor(previousBlock, block.header, blockHash, block.transactions.size)
  }
}

/** Wraps a block, maintains additional information such as chain work.
  *
  * We need to maintain the chain work(the total number of hash calculations from the genesis block up to a block) for each block.
  * Based on the chain work, we will decide the best blockchain.
  *
  * We will keep a tree of blocks by keeping the previous block in the current block.
  *
  * @param previousBlock The block descriptor of the previous block of the this block.
  * @param blockHash The hash of the block header.
  * @param blockHeader The header of the block.
  * @param transactionCount The number of transactions that the block has.
  */
case class BlockDescriptor(previousBlock : BlockDescriptor, blockHeader : BlockHeader, blockHash : BlockHash, transactionCount : Long) {
  /** The total number of hash calculations from the genesis block.
    */
  val chainWork : Long =
    (if (previousBlock == null) 0 else previousBlock.chainWork) +
    BlockDescriptor.getHashCalculations(blockHash)

  /** The height of the current block. The genesis block, whose previousBlock is null has height 0.
    */
  val height : Long =
    if (previousBlock == null) 0 else previousBlock.height + 1
}
