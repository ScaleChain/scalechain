package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.index._
import io.scalechain.crypto.HashEstimation
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 3/23/16.
  */
trait BlockStorage extends SharedKeyValueDatabase with BlockIndex with TransactionPool with OrphanBlockIndex with OrphanTransactionIndex {
  private val logger = LoggerFactory.getLogger(classOf[BlockStorage])
  protected[storage] val blockDatabase : BlockDatabase

  def putBlock(blockHash : Hash, block : Block) : Boolean
  def getTransaction(transactionHash : Hash) : Option[Transaction]
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)]

  def getTransactionDescriptor(txHash : Hash) : Option[TransactionDescriptor]
  def putTransactionDescriptor(txHash : Hash, transactionDescriptor : TransactionDescriptor)

  /** Get the block hash at the given height on the best blockchain.
    *
    * @param height The height of the block.
    * @return The hash of the block at the height on the best blockchain.
    */
  def getBlockHashByHeight(height : Long) : Option[Hash] = {
    // TODO : BUGBUG : Need to add synchronization?
    blockDatabase.getBlockHashByHeight(height)
  }

  /** Put the block hash searchable by height.
    *
    * @param height The height of the block hash. The block should be on the best blockchain.
    * @param hash The hash of the block.
    */
  def putBlockHashByHeight(height : Long, hash : Hash) : Unit = {
    // TODO : BUGBUG : Need to add synchronization?
    blockDatabase.putBlockHashByHeight(height, hash)
  }

  /** Update the hash of the next block.
    *
    * @param hash The block to update the next block hash.
    * @param nextBlockHash Some(nextBlockHash) if the block is on the best blockchain, None otherwise.
    */
  def updateNextBlockHash(hash : Hash, nextBlockHash : Option[Hash]) = {
    // TODO : BUGBUG : Need to add synchronization?
    blockDatabase.updateNextBlockHash(hash, nextBlockHash)
  }

  /** Get the hash of the next block.
    *
    * @param hash The hash of the block to get the next block of it. The block should exist on the block database.
    * @return Some(hash) if the given block hash is for a block on the best blockchain and not the best block. None otherwise.
    */
  def getNextBlockHash(hash : Hash) : Option[Hash] = {
    // TODO : BUGBUG : Need to add synchronization?
    blockDatabase.getBlockInfo(hash).get.nextBlockHash
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

  /** Put the block header hash of the tip on the best blockchain.
    *
    * @param hash The best block hash
    */
  def putBestBlockHash(hash : Hash) : Unit = {
    this.synchronized {
      blockDatabase.putBestBlockHash(hash)
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
