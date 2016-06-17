package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.proto.{BlockHeader, Hash, Block}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

object BlockProcessor extends BlockProcessor(Blockchain.get)

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
class BlockProcessor(val chain : Blockchain) {
  private val logger = LoggerFactory.getLogger(classOf[BlockProcessor])



  /** Get a block.
    *
    * @param blockHash The hash of the block to get.
    * @return Some(block) if the block exists; None otherwise.
    */
  def getBlock(blockHash : Hash) : Option[Block] = {
    chain.getBlock(blockHash).map(_._2)
  }

  /** Check if a block exists either as an orphan or non-orphan.
    * naming rule : 'exists' checks orphan blocks as well, whereas hasNonOrphan does not.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exist; false otherwise.
    */
  def exists(blockHash : Hash) : Boolean  = {
    return hasNonOrphan(blockHash) || hasOrphan(blockHash)
  }

  /** Check if we have the given block as an orphan.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exists as an orphan; false otherwise.
    */
  def hasOrphan(blockHash : Hash) : Boolean = {
    chain.blockOrphanage.hasOrphan(blockHash)
  }

  /** Check if the block exists as a non-orphan block.
    *
    * @param blockHash the hash of the block to check.
    * @return true if the block exists as a non-orphan; false otherwise.
    */
  def hasNonOrphan(blockHash : Hash) : Boolean = {
    chain.hasBlock(blockHash)
  }

  /** Put the block as an orphan block.
    *
    * @param block the block to put as an orphan block.
    */
  def putOrphan(block : Block) : Unit = {
    chain.blockOrphanage.putOrphan(block)
  }

  /**
    * Get the root orphan that does not have its parent even in the orphan blocks.
    * Ex> When B1's parent is B0, B2's parent is B1 and B0 is missing, the orphan root of the B2 is B1.
    *
    * @param blockHash The block to find the root parent of it.
    * @return The hash of the orphan block whose parent is missing even in the orphan blocks list.
    */
  def getOrphanRoot(blockHash : Hash) : Hash = {
    chain.blockOrphanage.getOrphanRoot(blockHash)
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
    // Step 8. Make sure that the same hash with the genesis transaction does not exist. If exists, throw an error saying that the coinbase data needs to have random data to make generation transaction id different from already existing ones.
    //    assert(false)
/*
    val message = s"The block is invalid(${outPoint})."
    logger.warn(message)
    throw new ChainException(ErrorCode.InvalidBlock, message)
*/
  }

  /**
    * Put the block into the blockchain. If a fork becomes the new best blockchain, do block reorganization.
    *
    * @param blockHash The hash of the block to accept.
    * @param block The block to accept.
    * @return true if the newly accepted block became the new best block.
    */
  def acceptBlock(blockHash : Hash, block : Block) : Boolean = {
    // Step 1. Need to check if the same blockheader hash exists by looking up mapBlockIndex
    // Step 2. Need to increase DoS score if an orphan block was received.
    // Step 3. Need to increase DoS score if the block hash does not meet the required difficulty.
    // Step 4. Need to get the median timestamp for the past N blocks.
    // Step 5. Need to check the lock time of all transactions.
    // Step 6. Need to check block hashes for checkpoint blocks.
    // Step 7. Write the block on the block database, reorganize blocks if necessary.
    chain.putBlock(blockHash, block)
  }

  /** Recursively accept orphan children blocks of the given block, if any.
    *
    * @param initialParentBlockHash The hash of the newly accepted parent block. As a result of it, we can accept the children of the newly accepted block.
    * @return A list of block hashes which were newly accepted.
    */
  def acceptChildren(initialParentBlockHash : Hash) : List[Hash] = {
    val acceptedChildren = new ArrayBuffer[Hash]

    var i = -1;
    do {
      val parentTxHash = if (acceptedChildren.length == 0) initialParentBlockHash else acceptedChildren(i)
      val dependentChildren : List[Hash] = chain.blockOrphanage.getOrphansDependingOn(parentTxHash)
      dependentChildren foreach { dependentChildHash : Hash =>
        val dependentChild = chain.blockOrphanage.getOrphan(dependentChildHash)
        assert(dependentChild.isDefined)

        // add to the transaction pool.
        acceptBlock(dependentChildHash, dependentChild.get)
        // add the hash to the acceptedChildren so that we can process children of the acceptedChildren as well.
        acceptedChildren.append(dependentChildHash)
        // delete the orphan
        chain.blockOrphanage.delOrphan(dependentChild.get)
      }
      chain.blockOrphanage.removeDependenciesOn(parentTxHash)
      i += 1
    } while( i < acceptedChildren.length)

    // Remove duplicate by converting to a set, and return as a list.
    acceptedChildren.toSet.toList
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


/* TODO : Need to implement getBlockHeader and acceptBlockHeader when we implement the headers-first approach.


  /** Get a block header
    *
    * @param blockHash The hash of the block to get.
    * @return Some(blockHeader) if the block header exists; None otherwise.
    */
  def getBlockHeader(blockHash : Hash) : Option[BlockHeader] = {
    chain.getBlockHeader(blockHash)
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
*/

}
