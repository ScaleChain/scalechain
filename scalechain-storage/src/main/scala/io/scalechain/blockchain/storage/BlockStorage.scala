package io.scalechain.blockchain.storage

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.index._
import io.scalechain.crypto.HashEstimation
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 3/23/16.
  */
trait BlockStorage extends BlockDatabase with BlockIndex with TransactionDescriptorIndex with TransactionPoolIndex with OrphanBlockIndex with OrphanTransactionIndex {
  private val logger = Logger( LoggerFactory.getLogger(classOf[BlockStorage]) )

  def putBlock(blockHash : Hash, block : Block)(implicit db : KeyValueDatabase) : List[TransactionLocator]
  def getTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Option[Transaction]
  def getBlock(blockHash : Hash)(implicit db : KeyValueDatabase) : Option[(BlockInfo, Block)]

  def close() : Unit

  /** Get the hash of the next block.
    *
    * @param hash The hash of the block to get the next block of it. The block should exist on the block database.
    * @return Some(hash) if the given block hash is for a block on the best blockchain and not the best block. None otherwise.
    */
  def getNextBlockHash(hash : Hash)(implicit db : KeyValueDatabase) : Option[Hash] = {
    // TODO : BUGBUG : Need to add synchronization?
    getBlockInfo(hash).get.nextBlockHash
  }

  def putBlock(block : Block)(implicit db : KeyValueDatabase) : List[TransactionLocator] = {
    putBlock(block.header.hash, block)
  }

  def putBlockHeader(blockHeader : BlockHeader)(implicit db : KeyValueDatabase) : Unit = {
    putBlockHeader(blockHeader.hash, blockHeader)
  }

  def hasBlock(blockHash : Hash)(implicit db : KeyValueDatabase) : Boolean = {
    val blockInfo = getBlockInfo(blockHash)
    blockInfo.isDefined && blockInfo.get.blockLocatorOption.isDefined
  }

  def hasTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Boolean = {
    // TODO : Optimize : We don't need to deserialize a transaction to see if it exists on our database.
    getTransaction(transactionHash).isDefined
  }

  def hasBlockHeader(blockHash : Hash)(implicit db : KeyValueDatabase) : Boolean = {
    getBlockHeader(blockHash).isDefined
  }

  def getBlockHeader(blockHash : Hash)(implicit db : KeyValueDatabase) : Option[BlockHeader] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      val blockInfoOption = getBlockInfo(blockHash)
      if (blockInfoOption.isDefined) {
        // case 1 : the block info was found.
        Some(blockInfoOption.get.blockHeader)
      } else {
        // case 2 : the block info was not found.
        None
      }
    }
  }

  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader)(implicit db : KeyValueDatabase) : Unit = {
    this.synchronized {
      // get the info of the previous block, to calculate the height of the given block and chain-work.
      val prevBlockInfoOption: Option[BlockInfo] = getBlockInfo(Hash(blockHeader.hashPrevBlock.value))

      // Either the previous block should exist or the block should be the genesis block.
      if (prevBlockInfoOption.isDefined || blockHeader.hashPrevBlock.isAllZero()) {

        val blockInfo: Option[BlockInfo] = getBlockInfo(blockHash)
        if (blockInfo.isEmpty) {

          // case 1.1 : the header does not exist yet.
          val blockInfo = BlockInfoFactory.create(
            // Pass None for the genesis block.
            prevBlockInfoOption,
            blockHeader,
            blockHash,
            0, // transaction count
            None // block locator
          )

          putBlockInfo(blockHash, blockInfo)
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
  }
}
