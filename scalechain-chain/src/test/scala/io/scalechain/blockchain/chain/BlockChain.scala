package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Block

/** Maintain the best blockchain, whose chain work is the biggest one.
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
class BlockChain {

  /** The descriptor of the best block.
    * This value is updated whenever a new best block is found.
    * We also have to check if we need to do block reorganization whenever this field is updated.
    */
  var theBestBlock : BlockDescriptor = null

  def putBlock(block:Block) : Unit = {
    // TODO : Implement
    assert(false)

    /*
    // Step 1 : Check if the previous block of the new block is the current best block.
    // Step 2 : We need block reorganization
    */
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
  * @param block The block we are going to wrap.
  */
case class BlockDescriptor(previousBlock : BlockDescriptor, block : Block) {
  /** The total number of hash calculations from the genesis block.
    */
  val chainWork : Long = previousBlock.chainWork + getHashCalculations(block)

  /** The height of the current block. The genesis block has height 0.
    */
  val height : Long = previousBlock.height + 1

  /** Calculate the estimated number of hash calculations for a block.
    *
    * @param block The block to calculate the estimated number of hashes.
    * @return The estimated number of hash calculations for the given block.
    */
  protected def getHashCalculations(block : Block) : Long = {
    // TODO : Implement
    assert(false)
    // Step 1 : Calculate the block hash

    // Step 2 : Calculate the (estimated) number of hash calculations based on the hash value.
    0
  }
}
