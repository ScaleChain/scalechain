package io.scalechain.blockchain.chain

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.ChainEnvironment
import org.slf4j.LoggerFactory

data class BlockLocatorHashes(val hashes : List<Hash>)

/**
  * The block locator that can produce the list of block hashes that another node requires.
  * Another node first sends the list of locator hashes by using getLocatorHashes,
  * and constructs GetBlocks message to get inventories of the missing block hashes.
  *
  * The receiver node finds out the common hash and produces a list of hashes sender needs.
  */
class BlockLocator(private val chain : Blockchain) {
  private val logger = LoggerFactory.getLogger(BlockLocator::class.java)

  /** Get the summary of block hashes that this node has.
    * We will use these hashes to create the GetBlocks request.
    *
    * @return The list of locator hashes summarizing the blockchain.
    */
  fun getLocatorHashes() : BlockLocatorHashes {
    // BUGBUG : We need to be able to get locator hashes from a block that is not on the best blockchain.
    // Ex> When we get headers to get the best blockchain that other nodes have,
    // the headers we get might not be on the best blockchain of this node.

    val env = ChainEnvironment.get()

    val listBuf = arrayListOf<Hash>()
    return chain.withTransaction { transactingDB ->
      var blockHeight = chain.getBestBlockHeight() // The height of the block we are processing.
      var addedHashes = 0 // The number of hashes added to the list.
      var heightSteps = 1 // For each loop, how may heights do we jump?
//println(s"blockHeight=${blockHeight}")
      while( blockHeight > 0 ) {
        // Step 1 : Add 10 recent block hashes on the best blockchain.
        listBuf.add( chain.getBlockHash(transactingDB, blockHeight) )
        addedHashes += 1

        // Step 2 : Exponentially move backwards to get a summarizing list of block hashes.
        if (addedHashes >= 10) {
          // Multiply heightSteps.
          heightSteps = heightSteps shl 1
        }
//println(s"heightSteps=${heightSteps}")

        blockHeight -= heightSteps
      }

      // Step 3 : Add the genesis block hash.
      listBuf.add( env.GenesisBlockHash )
      BlockLocatorHashes(listBuf)
    }
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
  fun getHashes(locatorHashes : BlockLocatorHashes, hashStop : Hash, maxHashCount : Int) : List<Hash> {
    val env = ChainEnvironment.get()
    val listBuf = arrayListOf<Hash>()

    // TODO : Optimize : Can we remove the chain.synchronized, as putBlock is atomic? ( May require using a RocksDB snapshot )
    return chain.withTransaction { transactingDB ->
      // Step 1 : Find any matching hash from the list of locator hashes
      // Use hashes.view instead of hashes to stop calling chain.hasBlock when we hit any matching hash on the chain.
      //
      // scala> listOf(1,2,3)
      // res0: List<Int> = listOf(1, 2, 3)
      //
      // scala> (res0.view.map{ i=> println(s"$i"); i *2 }).head
      // 1
      // res9: Int = 2

      val matchedHashOption : Hash? = locatorHashes.hashes.asSequence().filter { hash ->
        val blockInfoOption = chain.getBlockInfo( transactingDB, hash )
        // The block info exists, and the block is on the best block chain(nextBlockHash is defined)
        blockInfoOption?.nextBlockHash != null
      }.firstOrNull()

      // Step 2 : Construct a list of hashes from the matching hash until the hashStop matches, or 500 hashes are constructed.
      // If no hash matched, start from the genesis block.
      val startingHash = matchedHashOption ?: env.GenesisBlockHash

      val blockInfo = chain.getBlockInfo(transactingDB, startingHash)!!
      // The block should be on the best blockchain.
      assert(blockInfo.nextBlockHash != null)

      val bestBlockHeight = chain.getBestBlockHeight()
      var blockHeight = blockInfo.height
      if ( blockHeight > bestBlockHeight) {
        logger.error("Invalid block height. Block hash : ${startingHash}, info : ${blockInfo}, best height : ${bestBlockHeight}")
        assert(false)
      }

      var addedHashes = 0
      var lastHash : Hash? = null

      do {
        // TODO : BUGBUG : Make sure that getBlockHash returns a block hash even though the blockchain has the header of the block only without any transaction data.
        lastHash = chain.getBlockHash(transactingDB, blockHeight)
        listBuf.add( lastHash )
        addedHashes += 1
        blockHeight += 1
      } while( blockHeight <= bestBlockHeight && addedHashes < maxHashCount && lastHash != hashStop )

      listBuf
    }
  }
}
