package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.index.BlockDatabase
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 3/23/16.
  */
trait BlockStorage extends BlockIndex {
  private val logger = LoggerFactory.getLogger(classOf[BlockStorage])

  def putBlock(blockHash : Hash, block : Block) : Boolean
  def getTransaction(transactionHash : Hash) : Option[Transaction]
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)]

  /** Remove a transaction from the block storage.
    *
    * We need to remove a transaction that are stored in a block which is not in the best block chain any more.
    *
    * @param transactionHash The hash of the transaction to remove from the blockchain.
    */
  def removeTransaction(transactionHash : Hash) : Unit

  protected[storage] def blockDatabase() : BlockDatabase

  def putBlock(block : Block) : Boolean = {
    val blockHash = Hash( HashCalculator.blockHeaderHash(block.header) )

    putBlock(blockHash, block)
  }

  def putBlockHeader(blockHeader : BlockHeader) : Unit = {
    val blockHash = Hash(HashCalculator.blockHeaderHash(blockHeader))

    putBlockHeader(blockHash, blockHeader)
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

  // Methods that are extended from BlockIndex.
  def getBlock(blockHash : BlockHash) : Option[(BlockInfo, Block)] = {
    getBlock(Hash(blockHash.value))
  }

  def getTransaction(transactionHash : TransactionHash) : Option[Transaction] = {
    getTransaction(Hash(transactionHash.value))
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
      if (blockHash.isAllZero()) {
        // case 1 : The previous block of Genesis block
        // Because genesis block's height is 0, we need to return -1.
        Some(-1)
      } else {
        // case 2 : Non-genesis block.
        blockDatabase.getBlockHeight(blockHash)
      }
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

  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader) : Unit = {
    this.synchronized {
      // get the height of the previous block, to calculate the height of the given block.
      val prevBlockHeightOption: Option[Int] = getBlockHeight(Hash(blockHeader.hashPrevBlock.value))

      if (prevBlockHeightOption.isDefined) {
        // case 1 : the previous block header was found.
        val blockHeight = prevBlockHeightOption.get + 1

        assert(blockHeight >= 0)

        val blockInfo: Option[BlockInfo] = blockDatabase.getBlockInfo(blockHash)
        if (blockInfo.isEmpty) {
          // case 1.1 : the header does not exist yet.
          val blockInfo = BlockInfo(
            height = blockHeight,
            transactionCount = 0,
            // BUGBUG : Need to use enumeration
            status = 0,
            blockHeader = blockHeader,
            None
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
