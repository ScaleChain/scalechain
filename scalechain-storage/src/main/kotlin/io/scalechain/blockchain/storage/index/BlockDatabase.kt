package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.ByteArrayExt

object DB {
  val BLOCK_INFO = 'b'.toByte()
  val TRANSACTION = 't'.toByte()
  val BLOCK_FILE_INFO = 'f'.toByte()
  val LAST_BLOCK_FILE = 'l'.toByte()
  val BEST_BLOCK_HASH = 'B'.toByte()
  val BLOCK_HEIGHT = 'h'.toByte()

  // The disk-pool, which keeps transactions on disk instead of mempool.
  val TRANSACTION_POOL = 'd'.toByte()
  // The index from transaction creation time to the transaction hash.
  val TRANSACTION_TIME = 'e'.toByte()

  // A temporary transaction pool for checking transaction attach-ability while creating blocks.
  val TEMP_TRANSACTION_POOL = 'y'.toByte()
  // A temporary transaction time index for checking transaction attach-ability while creating blocks.
  val TEMP_TRANSACTION_TIME = 'z'.toByte()

  val ORPHAN_BLOCK = '1'.toByte()
  val ORPHAN_TRANSACTION = '2'.toByte()
  val ORPHAN_BLOCKS_BY_PARENT = '3'.toByte()
  val ORPHAN_TRANSACTIONS_BY_DEPENDENCY = '4'.toByte()
}

/** Maintains block chains with different height, it knows which one is the best one.
  *
  * This class is used by CassandraBlockStorage.
  */
interface BlockDatabase {
/*
  private val logger = LoggerFactory.getLogger(BlockDatabase::class.java)
*/
  fun getBlockInfo(db : KeyValueDatabase, hash : Hash) : BlockInfo? {
    return db.getObject(HashCodec, BlockInfoCodec, DB.BLOCK_INFO, hash)
  }

  /*
  // BUGBUG : Write unittest
  fun hasBlock(db : KeyValueDatabase, hash : Hash) : Boolean {
    return db.hasObject(HashCodec, DB.BLOCK_INFO, hash )
  }
  */

  /** Get the block hash at the given height on the best blockchain.
    *
    * @param height The height of the block.
    * @return The hash of the block at the height on the best blockchain.
    */
  fun getBlockHashByHeight(db : KeyValueDatabase, height : Long) : Hash? {
    return db.getObject(BlockHeightCodec, HashCodec, DB.BLOCK_HEIGHT, BlockHeight(height))
  }

  /** Put the block hash searchable by height.
    *
    * @param height The height of the block hash. The block should be on the best blockchain.
    * @param hash The hash of the block.
    */
  fun putBlockHashByHeight(db : KeyValueDatabase, height : Long, hash : Hash) : Unit {
    db.putObject(BlockHeightCodec, HashCodec, DB.BLOCK_HEIGHT, BlockHeight(height), hash)
  }

  /**
    * Del the block hash by height.
    *
    * @param height the height of the block to delete.
    */
  fun delBlockHashByHeight(db : KeyValueDatabase, height : Long) : Unit {
    db.delObject(BlockHeightCodec, DB.BLOCK_HEIGHT, BlockHeight(height))
  }

  /** Update the hash of the next block.
    *
    * @param hash The block to update the next block hash.
    * @param nextBlockHash Some(nextBlockHash) if the block is on the best blockchain, None otherwise.
    */
  fun updateNextBlockHash(db : KeyValueDatabase, hash : Hash, nextBlockHash : Hash?) {
    val blockInfoOption : BlockInfo? = getBlockInfo(db, hash)
    assert(blockInfoOption != null)
    putBlockInfo(db, hash, blockInfoOption!!.copy(
      nextBlockHash = nextBlockHash
    ))
  }

  fun getBlockHeight(db : KeyValueDatabase, hash : Hash) : Long? {
    return getBlockInfo(db, hash)?.height
  }

  fun putBlockInfo(db : KeyValueDatabase, hash : Hash, info : BlockInfo) : Unit {
    val blockInfoOption = getBlockInfo(db, hash)
    if (blockInfoOption != null) {
      val currentBlockInfo = blockInfoOption
      // hit an assertion : put a block info with different height
      assert(currentBlockInfo.height == info.height)

      // hit an assertion : put a block info with a different block locator.
      if (info.blockLocatorOption != null) {
        if ( currentBlockInfo.blockLocatorOption != null ) {
          assert( currentBlockInfo.blockLocatorOption == info.blockLocatorOption )
        }
      }

      // hit an assertion : change any field on the block header
      assert(currentBlockInfo.blockHeader == info.blockHeader)
    }

    db.putObject(HashCodec, BlockInfoCodec, DB.BLOCK_INFO, hash, info)
  }

  fun putBestBlockHash(db : KeyValueDatabase, hash : Hash) : Unit {
    db.putObject(HashCodec, ByteArrayExt.from(DB.BEST_BLOCK_HASH), hash)
  }

  fun getBestBlockHash(db : KeyValueDatabase) : Hash? {
    return db.getObject(HashCodec, ByteArrayExt.from(DB.BEST_BLOCK_HASH))
  }
}

/** BlockDatabase for use with RecordStorage.
  *
  * Additional features : tracking block file info
  *
  * When storing blocks with RecordStorage, we need to keep track of block file information.
  */
interface BlockDatabaseForRecordStorage : BlockDatabase {
  fun putBlockFileInfo(db : KeyValueDatabase, fileNumber : FileNumber, blockFileInfo : BlockFileInfo) : Unit {
    // Input validation for the block file info.
    val currentInfoOption = getBlockFileInfo(db, fileNumber)
    if (currentInfoOption != null) {
      val currentInfo = currentInfoOption
      // Can't put the same block info twice.
      assert( currentInfo != blockFileInfo )

      // First block height can't be changed.
      assert( currentInfo.firstBlockHeight == blockFileInfo.firstBlockHeight)

      // First block timestamp can't be changed.
      assert( currentInfo.firstBlockTimestamp == blockFileInfo.firstBlockTimestamp)

      // Block count should not be decreased.
      // Block count should increase
      assert( currentInfo.blockCount < blockFileInfo.blockCount)

      // File size should not be decreased
      // File size should increase
      assert( currentInfo.fileSize < blockFileInfo.fileSize )


      // The last block height should not be decreased.
      // The last block height should increase

// when a orphan block is
//      assert( currentInfo.lastBlockHeight < blockFileInfo.lastBlockHeight)

      // Caution : The last block timestamp can decrease.
    }

    db.putObject(FileNumberCodec, BlockFileInfoCodec, DB.BLOCK_FILE_INFO, fileNumber, blockFileInfo)
  }

  fun getBlockFileInfo(db : KeyValueDatabase, fileNumber : FileNumber) : BlockFileInfo? {
    return db.getObject(FileNumberCodec, BlockFileInfoCodec, DB.BLOCK_FILE_INFO, fileNumber)
  }

  fun putLastBlockFile(db : KeyValueDatabase, fileNumber : FileNumber) : Unit {
    // Input validation check for the fileNumber.
    val fileNumberOption = getLastBlockFile(db)
    if (fileNumberOption != null) {
      // The file number should increase.
      assert( fileNumberOption.fileNumber < fileNumber.fileNumber )
    }

    db.putObject(FileNumberCodec, ByteArrayExt.from(DB.LAST_BLOCK_FILE), fileNumber)
  }

  fun getLastBlockFile(db : KeyValueDatabase) : FileNumber? {
    return db.getObject(FileNumberCodec, ByteArrayExt.from(DB.LAST_BLOCK_FILE))
  }

}