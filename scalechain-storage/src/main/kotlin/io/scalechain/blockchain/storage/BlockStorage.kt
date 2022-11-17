package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.*
import io.scalechain.blockchain.storage.index.*
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 3/23/16.
  */
interface BlockStorage : BlockDatabase, BlockIndex, TransactionDescriptorIndex, TransactionPoolIndex, TransactionTimeIndex, OrphanBlockIndex, OrphanTransactionIndex {

  fun putBlock(db : KeyValueDatabase, blockHash : Hash, block : Block) : Unit

  fun close() : Unit

  /** Get the hash of the next block.
    *
    * @param hash The hash of the block to get the next block of it. The block should exist on the block database.
    * @return Some(hash) if the given block hash is for a block on the best blockchain and not the best block. None otherwise.
    */
  fun getNextBlockHash(db : KeyValueDatabase, hash : Hash) : Hash? {
    // TODO : BUGBUG : Need to add synchronization?
    return getBlockInfo(db, hash)?.nextBlockHash
  }

  fun putBlock(db : KeyValueDatabase, block : Block) : Unit {
    putBlock(db, block.header.hash(), block)
  }

  fun putBlockHeader(db : KeyValueDatabase, blockHeader : BlockHeader) : Unit {
    putBlockHeader(db, blockHeader.hash(), blockHeader)
  }

  fun hasBlock(db : KeyValueDatabase, blockHash : Hash) : Boolean {
    val blockInfo = getBlockInfo(db, blockHash)
    return blockInfo?.blockLocatorOption != null
  }

  fun hasTransaction(db : KeyValueDatabase, transactionHash : Hash) : Boolean {
    // TODO : Optimize : We don't need to deserialize a transaction to see if it exists on our database.
    return getTransaction(db, transactionHash) != null
  }

  fun hasBlockHeader(db : KeyValueDatabase, blockHash : Hash) : Boolean {
    return getBlockHeader(db, blockHash) != null
  }

  fun getBlockHeader(db : KeyValueDatabase, blockHash : Hash) : BlockHeader? {
    // TODO : Check if we need synchronization

    val blockInfoOption = getBlockInfo(db, blockHash)
    if (blockInfoOption != null) {
      // case 1 : the block info was found.
      return blockInfoOption.blockHeader
    } else {
      // case 2 : the block info was not found.
      return null
    }
  }

  fun putBlockHeader(db : KeyValueDatabase, blockHash : Hash, blockHeader : BlockHeader) : Unit {
    // TODO : Check if we need synchronization

    // get the info of the previous block, to calculate the height of the given block and chain-work.
    val prevBlockInfoOption: BlockInfo? = getBlockInfo(db, Hash(blockHeader.hashPrevBlock.value))

    // Either the previous block should exist or the block should be the genesis block.
    if (prevBlockInfoOption != null || blockHeader.hashPrevBlock.isAllZero()) {

      val blockInfo: BlockInfo? = getBlockInfo(db, blockHash)
      if (blockInfo == null) {

        // case 1.1 : the header does not exist yet.
        val newBlockInfo = BlockInfoFactory.create(
          // Pass None for the genesis block.
          prevBlockInfoOption,
          blockHeader,
          blockHash,
          0, // transaction count
          null // block locator
        )

        putBlockInfo(db, blockHash, newBlockInfo)
        // We are not checking if the block is the best block, because we received a header only.
        // We put a block as a best block only if we have the block data as long as the header.
      } else {
        // case 1.2 : the same block header already exists.
        logger.trace("A block header is put onto the block database twice. block hash : {}", blockHash)

        // blockIndex hits an assertion if the block header is changed for the same block hash.
        // TODO : Need to change to throw an exception if we try to overwrite with a different block header.
        //blockIndex.putBlockInfo(blockHash, blockInfo.get.copy(
        //  blockHeader = blockHeader
        //))

      }
    } else {
      // case 2 : the previous block header was not found.
      logger.trace("An orphan block was discarded while saving a block header. block header : {}", blockHeader)
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(BlockStorage::class.java)
  }
}
