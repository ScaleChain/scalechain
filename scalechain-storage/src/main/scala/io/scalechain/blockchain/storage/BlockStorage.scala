package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.index.{KeyValueDatabase, RocksDatabase, BlockDatabase}
import io.scalechain.crypto.HashEstimation
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 3/23/16.
  */
trait BlockStorage extends BlockIndex {
  private val logger = LoggerFactory.getLogger(classOf[BlockStorage])
  protected[storage] val keyValueDB : KeyValueDatabase
  protected[storage] val blockDatabase : BlockDatabase

  def putBlock(blockHash : Hash, block : Block) : Boolean
  def getTransaction(transactionHash : Hash) : Option[Transaction]
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)]

  def getBlockHash(blockHeight : Long) : Option[Hash] = {
    // TODO : Implement
    assert(false)
    return null
  }

  /** Remove a transaction from the block storage.
    *
    * We need to remove a transaction that are stored in a block which is not in the best block chain any more.
    *
    * @param transactionHash The hash of the transaction to remove from the blockchain.
    */
  def removeTransaction(transactionHash : Hash) : Unit

  def putBlock(block : Block) : Boolean = {
    putBlock(block.header.hash, block)
  }

  def putBlockHeader(blockHeader : BlockHeader) : Unit = {
    putBlockHeader(blockHeader.hash, blockHeader)
  }

  def hasBlock(blockHash : Hash) : Boolean = {
    // TODO : Optimize : We don't need to deserialize a block to see if it exists on our database.
    getBlock(blockHash).isDefined
  }

  def hasTransaction(transactionHash : Hash) : Boolean = {
    // TODO : Optimize : We don't need to deserialize a transaction to see if it exists on our database.
    getTransaction(transactionHash).isDefined
  }

  def hasBlockHeader(blockHash : Hash) : Boolean = {
    getBlockHeader(blockHash).isDefined
  }

  protected[storage] var bestBlockHeightOption : Option[Int] = None

  protected[storage] def checkBestBlockHash(blockHash : Hash, height : Int): Unit = {
    if (bestBlockHeightOption.isEmpty) {
      bestBlockHeightOption = blockDatabase.getBestBlockHash().map(blockDatabase.getBlockHeight(_).get)
    }

    if (bestBlockHeightOption.isEmpty || bestBlockHeightOption.get < height) { // case 1 : the block height of the new block is greater than the highest one.
      bestBlockHeightOption = Some(height)
      blockDatabase.putBestBlockHash(blockHash)
    } else { // case 2 : the block height of the new block is less than the highest one.
      // do nothing
    }
  }

  protected[storage] def getBlockHeight(blockHash : Hash) : Option[Int] = {
    this.synchronized {
      blockDatabase.getBlockHeight(blockHash)
    }
  }

  /** Get the block header hash of the tip on the best blockchain.
    *
    * @return The best block hash.
    */
  def getBestBlockHash() : Option[Hash] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      blockDatabase.getBestBlockHash()
    }
  }

  def getBlockHeader(blockHash : Hash) : Option[BlockHeader] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      val blockInfoOption = blockDatabase.getBlockInfo(blockHash)
      if (blockInfoOption.isDefined) {
        // case 1 : the block info was found.
        Some(blockInfoOption.get.blockHeader)
      } else {
        // case 2 : the block info was not found.
        None
      }
    }
  }

  /** Get the information about the block, such as block height etc.
    *
    * @param blockHash The hash of the block header to get the information.
    * @return The information about the block.
    */
  def getBlockInfo(blockHash : Hash) : Option[BlockInfo] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      blockDatabase.getBlockInfo(blockHash)
    }
  }

  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader) : Unit = {
    this.synchronized {
      // get the info of the previous block, to calculate the height of the given block and chain-work.
      val prevBlockInfoOption: Option[BlockInfo] = getBlockInfo(Hash(blockHeader.hashPrevBlock.value))

      // Either the previous block should exist or the block should be the genesis block.
      if (prevBlockInfoOption.isDefined || blockHeader.hashPrevBlock.isAllZero()) {

        val blockInfo: Option[BlockInfo] = blockDatabase.getBlockInfo(blockHash)
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

          blockDatabase.putBlockInfo(blockHash, blockInfo)
          // We are not checking if the block is the best block, because we received a header only.
          // We put a block as a best block only if we have the block data as long as the header.
        } else {
          // case 1.2 : the same block header already exists.
          logger.warn("A block header is put onto the block database twice. block hash : {}", blockHash)

          // blockIndex hits an assertion if the block header is changed for the same block hash.
          // TODO : Need to change to throw an exception if we try to overwrite with a different block header.
          //blockIndex.putBlockInfo(blockHash, blockInfo.get.copy(
          //  blockHeader = blockHeader
          //))

        }
      } else {
        // case 2 : the previous block header was not found.
        logger.warn("An orphan block was discarded while saving a block header. block header : {}", blockHeader)
      }
    }
  }

  def close() : Unit
}
