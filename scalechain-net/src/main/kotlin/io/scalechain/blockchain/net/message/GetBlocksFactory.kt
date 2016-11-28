package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.chain.BlockLocator
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.BlockLocatorHashes
import io.scalechain.blockchain.proto.GetBlocks
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.BlockInfo
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.ChainEnvironment

/**
  *  The factory that creates GetBlocks messages.
  */
object GetBlocksFactory {
  /** Create a GetBlocks message to get the given block.
    *
    * @param blockHashToGet The hash of the block to get.
    * @return
    */
  fun create(blockHashToGet : Hash = Hash.ALL_ZERO) : GetBlocks {
    val env = ChainEnvironment.get
    implicit val db : KeyValueDatabase = Blockchain.get.db

    val locator = BlockLocator(Blockchain.get)
    val blockLocatorHashes = locator.getLocatorHashes().hashes
    GetBlocks(env.DefaultBlockVersion, blockLocatorHashes, blockHashToGet)
  }
}
