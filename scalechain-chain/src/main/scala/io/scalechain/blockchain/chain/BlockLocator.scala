package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Hash


case class BlockLocatorHashes(hashes : List[Hash])

/**
  * The block locator that can produce the list of block hashes that another node requires.
  * Another node first sends the list of locator hashes by using getLocatorHashes,
  * and constructs GetBlocks message to get inventories of the missing block hashes.
  *
  * The receiver node finds out common hash and produces a list of hashes it needs
  * finds the common block between another node's blockchain and my blockchain.
  */
class BlockLocator(blockchain : Blockchain) {

  // getHashes returns MAX_HASH_COUNT is the stop hash is all zero.
  val MAX_HASH_COUNT = 500

  /** Get the summary of block hashes that this node has.
    * We will use these hashes to create the GetBlocks request.
    *
    * @return The list of locator hashes summarizing the blockchain.
    */
  def getLocatorHashes() : BlockLocatorHashes = {
    // Step 1 : Add 10 recent block hashes on the best blockchain.

    // Step 2 : Exponentially move backwards to get a summarizing list of block hashes.

    // Step 3 : Add the genesis block hash.

    // TODO : Implement
    assert(false)
    null
  }

  /** Get a list of hashes from the hash that matches one in locatorHashes.
    * c.f. The locator Hashes is constructed by another node using getLocatorHashes, and sent to this node via GetBlocks message.
    * Recent hashes come first in the locatorHashes list.
    *
    * We will use the result of this method to get the list of hashes to send to the requester of GetBlocks.
    *
    * @param locatorHashes The list of hashes to find from blockchain.
    *                      If any matches, we start constructing a list of hashes from it.
    * @param hashStop While constructing the list of hashes, stop at this hash if the hash matches.
    */
  def getHashes(locatorHashes : BlockLocatorHashes, hashStop : Hash) : List[Hash] = {
    // Step 1 : Find any matching hash from the list of locator hashes

    // Step 2 : Construct a list of hashes from the matching hash until the hashStop matches, or 500 hashes are constructed.

    // TODO : Implement
    assert(false)
    null
  }
}
