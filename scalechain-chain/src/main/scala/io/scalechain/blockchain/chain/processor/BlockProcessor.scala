package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.proto.{BlockHeader, Hash, Block}

/** Process a received block.
  *
  * The block processor is responsible for following handlings.
  *
  * Case 1. Orphan block handling.
  *    - Need to put the block as an orphan blocks if the parent block does not exist yet.
  *    - Need to request to get parents of the orphan blocks to the sender of the block.
  *    - Need to recursively move orphan blocks into non-orphan blocks if the parent of them is newly added.
  *
  * Case 2. Block reorganization.
  *    - If a new best blockchain with greater chain work(=the estimated number of hash calculations) is found,
  *      We need to reorganize blocks and transactions in them.
  *
  * Case 3. Append to the best blockchain.
  *    - If the parent of the block is the current best block(=the tip of the best blockchain), put the block on top of the current best block.
  *
  */
object BlockProcessor {
  val chain = Blockchain.get

  /** Get a block.
    *
    * @param blockHash The hash of the block to get.
    * @return Some(block) if the block exists; None otherwise.
    */
  def getBlock(blockHash : Hash) : Option[Block] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get a block header
    *
    * @param blockHash The hash of the block to get.
    * @return Some(blockHeader) if the block header exists; None otherwise.
    */
  def getBlockHeader(blockHash : Hash) : Option[BlockHeader] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Check if a block exists either as an orphan or non-orphan.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exist; false otherwise.
    */
  def hasBlock(blockHash : Hash) : Boolean  = {
    return hasNonOrphan(blockHash) || hasOrphan(blockHash)
  }

  /** Check if we have the given block as an orphan.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exists as an orphan; false otherwise.
    */
  def hasOrphan(blockHash : Hash) : Boolean = {
    // TODO : Implement
    assert(false)
    false
  }

  /** Check if the block exists as a non-orphan block.
    *
    * @param blockHash the hash of the block to check.
    * @return true if the block exists as a non-orphan; false otherwise.
    */
  def hasNonOrphan(blockHash : Hash) : Boolean = {
    // TODO : Implement
    assert(false)
    false
  }

  /** Put the block as an orphan block.
    *
    * @param block the block to put as an orphan block.
    */
  def putOrphan(block : Block) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /**
    * Get the root parent that is missing for the given block.
    * Ex> When B1's parent is B0, B2's parent is B1 and B0 is missing, the orphan root of the B2 is B0.
    *
    * @param blockHash The block to find the root parent of it.
    * @return The hash of the (missing) root parent.
    */
  def getOrphanRoot(blockHash : Hash) : Hash = {
    // TODO : Implement
    assert(false)
    null
  }

  /**
    * Validate a block.
    *
    * @param block
    * @return
    */
  def validateBlock(block : Block) : Unit = {
    // Step 1. check the serialized block size.
    // Step 2. check the proof of work - block hash vs target hash
    // Step 3. check the block timestamp.
    // Step 4. check the first transaction is coinbase, and others are not.
    // Step 5. check each transaction in a block.
    // Step 6. check the number of script operations on the locking/unlocking script.
    // Step 7. Calculate the merkle root hash, compare it with the one in the block header.
    // TODO : Implement
/*
    val message = s"The block is invalid(${outPoint})."
    logger.warn(message)
    throw new ChainException(ErrorCode.InvalidBlock, message)
*/
    assert(false)
  }

  /**
    * Put the block into the blockchain. If a fork becomes the new best blockchain, do block reorganization.
    *
    * @param blockHash The hash of the block to accept.
    * @param block The block to accept.
    * @return true if the newly accepted block became the new best block.
    */
  def acceptBlock(blockHash : Hash, block : Block) : Boolean = {
    // TODO : Implement
    assert(false)
    true

    // Step 1. Need to check if the same blockheader hash exists by looking up mapBlockIndex
    // Step 2. Need to increase DoS score if an orphan block was received.
    // Step 3. Need to increase DoS score if the block hash does not meet the required difficulty.
    // Step 4. Need to get the median timestamp for the past N blocks.
    // Step 5. Need to check the lock time of all transactions.
    // Step 6. Need to check block hashes for checkpoint blocks.
    // Step 7. Write the block on the block database, reorganize blocks if necessary.
    // on Step 7, call chain.putBlock.
  }

  /**
    * Remove the block from the orphan blocks.
    *
    * @param block The block to delete from orphans.
    */
  protected[chain] def delOrphan(block : Block) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Recursively accept orphan children blocks of the given block, if any.
    *
    * @param block The newly accepted parent block. As a result of it, we can accept the children of the newly accepted block.
    * @return A list of block hashes, which were the best block by the time they were accepted.
    */
  def acceptChildren(block : Block) : List[Hash] = {
    // TODO : Implement
    assert(false)
    null
/*
    newly_added_blocks = List(block hash)
    LOOP newBlock := For each newly_added_blocks
      LOOP orphanBlock := For each orphan block which depends on the new Block as the parent of it
    // Store the block into the blockchain database.
    if (orphanBlock->AcceptBlock())
      newly_added_blocks += orphanBlock.hash
    remove the orphanBlock from mapOrphanBlocks
    remove all orphan blocks depending on newBlock from mapOrphanBlocksByPrev
*/
  }


  /** Accept the block header to the blockchain.
    *
    * @param blockHeader The block header to accept.
    */
  def acceptBlockHeader(blockHeader :BlockHeader ) : Unit = {
    // Step 1 : Check if the block header already exists, return the block index of it if it already exists.
    // Step 2 : Check the proof of work and block timestamp.

    // Step 3 : Get the block index of the previous block.
    // Step 4 : Check proof of work, block timestamp, block checkpoint, block version based on majority of recent block versions.

    // Step 5 : Add the new block as a block index.
    // TODO : Implement
    assert(false)
  }
}
