package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.BlockHeader
import io.scalechain.blockchain.proto.OrphanBlockDescriptor
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.BlockStorage
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.index.KeyValueDatabase

/**
  * Created by kangmo on 6/9/16.
  */
class BlockOrphanage(private val storage : BlockStorage) {
  /** Check if we have the given block as an orphan.
    *
    * @param blockHash The hash of the block to check.
    * @return true if the block exists as an orphan; false otherwise.
    */
  fun hasOrphan(db : KeyValueDatabase, blockHash : Hash) : Boolean {
    return storage.getOrphanBlock(db, blockHash) != null
  }

  /** Put the block as an orphan block.
    *
    * @param block the block to put as an orphan block.
    */
  fun putOrphan(db : KeyValueDatabase, block : Block) : Unit {
    // TODO : BUGBUG : Need to check the max size of RocksDB value, as a block can grow to a big size such as 100MB ?
    // TOOD : BUGBUG : Need to check DoS attack consuming disk storage by sending orphan blocks.
    // TODO : Optimize : Use headers-first approach, or drop orphan blocks.
    // TODO : Need to have maximum number of orphan blocks?
    val blockHash = block.header.hash()
    storage.putOrphanBlock(db, blockHash, OrphanBlockDescriptor(block))
    storage.addOrphanBlockByParent(db, block.header.hashPrevBlock, blockHash)
  }


  /**
    * Get an orphan block.
    *
    * @param blockHash The hash of the orphan block.
    * @return Some(block) if the orphan was found; None otherwise.
    */
  fun getOrphan(db : KeyValueDatabase, blockHash : Hash) : Block? {
    return storage.getOrphanBlock(db, blockHash)?.block
  }

  /**
    * Remove the block from the orphan blocks.
    *
    * @param block The block to delete from orphans.
    */
  fun delOrphan(db : KeyValueDatabase, block : Block) : Unit {
    storage.delOrphanBlock(db, block.header.hash())
  }



  tailrec protected fun getOrphanRoot(db : KeyValueDatabase, blockHeader : BlockHeader) : Hash {
    val blockHash = blockHeader.hash()
    assert(blockHash != blockHeader.hashPrevBlock)
    val parentOrphanOption = storage.getOrphanBlock(db, blockHeader.hashPrevBlock)
    if (parentOrphanOption == null) { // The base case. The parent does not exist in the orphanage.
      return blockHash
    } else { // We still have the parent in the orphanage.
      return getOrphanRoot( db, parentOrphanOption.block.header )
    }
  }

  /**
    * Get the root orphan that does not have its parent even in the orphan blocks.
    * Ex> When B1's parent is B0, B2's parent is B1 and B0 is missing, the orphan root of the B2 is B1.
    *
    * @param blockHash The block to find the root parent of it.
    * @return The hash of the orphan block whose parent is missing even in the orphan blocks list.
    */
  fun getOrphanRoot(db : KeyValueDatabase, blockHash : Hash) : Hash {
    val orphanOption = storage.getOrphanBlock(db, blockHash)
    // getOrphanRoot should never called to a non-orphan block.
    // TODO : Make sure that concurrent threads can't delete the orphan block while getOrphanRoot is called.

    return getOrphanRoot(db, orphanOption!!.block.header)
  }

  /** Get the list of orphan block hashes depending a given block.
    *
    * @param blockHash The block that orphans are depending on.
    * @return The list of orphan block hashes depending the given block.
    */
  fun getOrphansDependingOn(db : KeyValueDatabase, blockHash : Hash) : List<Hash> {
    return storage.getOrphanBlocksByParent(db, blockHash)
  }


  /** Remove dependencies on an orphan parent.
    *
    * @param blockHash The parent block hash.
    */
  fun removeDependenciesOn(db : KeyValueDatabase, blockHash : Hash) : Unit {
    storage.delOrphanBlocksByParent(db, blockHash)
  }
}
