package io.scalechain.blockchain.chain.mining

import io.scalechain.blockchain.chain.BlockDescriptor
import io.scalechain.blockchain.proto.{Block, BlockHeader, MerkleRootHash, Transaction}

/** The template of a block for creating a block.
  * It has list of transactions to put into a block.
  *
  * The transactions are sorted, and they are chosen from the mempool based on (1) priority and (2) fee.
  * We need to sort the transactions, and calculate a block header for finding out the nonce that produces a block header
  * which is less than or equal to the minimum block header hash calculated from the difficulty bits in the block header.
  *
  * @param difficultyBits the 4 byte integer representing the hash difficulty. This value is stored as the block header's target.
  * @param sortedTransactions the sorted transactions to add to the block.
  *
  */
class BlockTemplate(difficultyBits : Long, sortedTransactions : List[Transaction]) {
  /** Calculate the merkle root hash from the sorted transactions.
    *
    * @return the merkle root hash.
    */
  def calculateMerkleRoot() : MerkleRootHash = {
    // TODO : Implement
    assert(false)

    // Step 1 : Sort

    null
  }

  /** Get the block header from this template.
    *
    * @param prevBlockDesc the descriptor of the previous block
    * @return The block header created from this template.
    */
  def getBlockHeader(prevBlockDesc : BlockDescriptor) : BlockHeader = {
    // Step 1 : Calculate the merkle root hash.
    val merkleRootHash = calculateMerkleRoot()

    // Step 2 : Create the block header
    BlockHeader(Block.VERSION, prevBlockDesc.blockHash, merkleRootHash, System.currentTimeMillis(), difficultyBits, 0L)
  }

  /** Find the nonce value by trying N times.
    *
    * @param blockHeader The header of the block.
    * @param tryCount The number of times to try to find a nonce.
    * @return Some nonce if the nonce was found. None otherwise.
    */
  def findNonce(blockHeader : BlockHeader, tryCount : Int) : Option[Long] = {
    // TODO : Implement
    assert(false)
    None
  }

  /** Create a block based on the block header and nonce.
    *
    * @param blockHeader The block header we got by calling getBlockHeader method.
    * @param nonce The nonce we found by calling findNonce method.
    * @return The created block that has all transactions in this template with a valid block header.
    */
  def createBlock(blockHeader : BlockHeader, nonce : Long) = {
    Block( blockHeader.copy(nonce = nonce),
      sortedTransactions )
  }
}