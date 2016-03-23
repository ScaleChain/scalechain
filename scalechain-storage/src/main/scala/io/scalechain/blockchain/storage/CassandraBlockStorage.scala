package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashCalculator

/**
  * Created by kangmo on 3/23/16.
  */
class CassandraBlockStorage(directoryPath : File) extends BlockStorage {

  /** Store a block.
    *
    * @param blockHash the hash of the header of the block to store.
    * @param block the block to store.
    *
    * @return Boolean true if the block header or block was not existing, and it was put for the first time. false otherwise.
    *                 submitblock rpc uses this method to check if the block to submit is a new one on the database.
    */
  def putBlock(blockHash : Hash, block : Block) : Boolean = {
    this.synchronized {
      // TODO : Implement
      assert(false)
      false
/*
      val blockInfo: Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)
      var isNewBlock = false

      val txLocators: List[TransactionLocator] =
        if (blockInfo.isDefined) {
          // case 1 : block info was found
          if (blockInfo.get.blockLocatorOption.isEmpty) {
            // case 1.1 : block info without a block locator was found
            val appendResult = blockWriter.appendBlock(block)
            val newBlockInfo = blockInfo.get.copy(
              transactionCount = block.transactions.size,
              blockLocatorOption = Some(appendResult.blockLocator)
            )
            blockIndex.putBlockInfo(blockHash, newBlockInfo)
            val fileSize = blockRecordStorage.files(appendResult.headerLocator.fileIndex).size
            updateFileInfo(appendResult.headerLocator, fileSize, newBlockInfo.height, block.header.timestamp)
            checkBestBlockHash(blockHash, newBlockInfo.height)

            //logger.info("The block locator was updated. block hash : {}", blockHash)
            appendResult.txLocators
          } else {
            // case 1.2 block info with a block locator was found
            // The block already exists. Do not put it once more.
            logger.warn("The block already exists. block hash : {}", blockHash)

            List()
          }
        } else {
          // case 2 : no block info was found.
          // get the height of the previous block, to calculate the height of the given block.
          val prevBlockHeightOption: Option[Int] = getBlockHeight(Hash(block.header.hashPrevBlock.value))

          if (prevBlockHeightOption.isDefined) {
            // case 2.1 : no block info was found, previous block header exists.
            val appendResult = blockWriter.appendBlock(block)

            val blockHeight = prevBlockHeightOption.get + 1
            val blockInfo = BlockInfo(
              height = blockHeight,
              transactionCount = block.transactions.size,
              // BUGBUG : Need to use enumeration
              status = 0,
              blockHeader = block.header,
              Some(appendResult.blockLocator)
            )
            blockIndex.putBlockInfo(blockHash, blockInfo)

            val fileSize = blockRecordStorage.files(appendResult.headerLocator.fileIndex).size
            updateFileInfo(appendResult.headerLocator, fileSize, blockInfo.height, block.header.timestamp)
            checkBestBlockHash(blockHash, blockHeight)

            isNewBlock = true
            //logger.info("The new block was put. block hash : {}", blockHash)

            appendResult.txLocators
          } else {
            // case 2.2 : no block info was found, previous block header does not exists.
            logger.warn("An orphan block was discarded while saving a block. block hash : {}", block.header)

            List()
          }
        }

      if (!txLocators.isEmpty) {
        // case 1.1 and case 2.1 has newly stored transactions.
        blockIndex.putTransactions(txLocators)
      }

      isNewBlock
*/
    }
  }

  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader) : Unit = {
    this.synchronized {
      // TODO : Implement
      assert(false)
/*
      // get the height of the previous block, to calculate the height of the given block.
      val prevBlockHeightOption: Option[Int] = getBlockHeight(Hash(blockHeader.hashPrevBlock.value))

      if (prevBlockHeightOption.isDefined) {
        // case 1 : the previous block header was found.
        val blockHeight = prevBlockHeightOption.get + 1

        assert(blockHeight >= 0)

        val blockInfo: Option[BlockInfo] = blockIndex.getBlockInfo(blockHash)
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
          blockIndex.putBlockInfo(blockHash, blockInfo)
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
*/
    }
  }

  def getTransaction(transactionHash : Hash) : Option[Transaction] = {
    this.synchronized {
      /*
      val txLocatorOption = blockIndex.getTransactionLocator(transactionHash)
      txLocatorOption.map(blockRecordStorage.readRecord(_)(TransactionCodec))
      */
      // TODO : Implement
      assert(false)
      None
    }
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)] = {
    this.synchronized {
      // TODO : Implement
      assert(false)
      None

      /*
      val blockInfoOption = blockIndex.getBlockInfo(blockHash)
      if (blockInfoOption.isDefined) {
        // case 1 : The block info was found.
        if (blockInfoOption.get.blockLocatorOption.isDefined) {
          //logger.info(s"getBlock - Found a block info with a locator. block hash : ${blockHash}, locator : ${blockInfoOption.get.blockLocatorOption}")
          // case 1.1 : the block info with a block locator was found.
          Some( (blockInfoOption.get, blockRecordStorage.readRecord(blockInfoOption.get.blockLocatorOption.get)(BlockCodec)) )
        } else {
          // case 1.2 : the block info without a block locator was found.
          //logger.info("getBlock - Found a block info without a locator. block hash : {}", blockHash)
          None
        }
      } else {
        // case 2 : The block info was not found
        //logger.info("getBlock - No block info found. block hash : {}", blockHash)
        None
      }
      */
    }
  }

  def getBestBlockHash() : Option[Hash] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      // TODO : Implement
      assert(false)
      None
/*
      blockIndex.getBestBlockHash()
*/
    }
  }

  def getBlockHeader(blockHash : Hash) : Option[BlockHeader] = {
    // TODO : Refactor : Remove synchronized.
    // APIs threads calling TransactionVerifier.verify and BlockProcessor actor competes to access DiskBlockDatabase.
    this.synchronized {
      // TODO : Implement
      assert(false)
      None
/*
      val blockInfoOption = blockIndex.getBlockInfo(blockHash)
      if (blockInfoOption.isDefined) {
        // case 1 : the block info was found.
        Some(blockInfoOption.get.blockHeader)
      } else {
        // case 2 : the block info was not found.
        None
      }
*/
    }
  }

  def close() : Unit = {
    // TODO : Implement
    assert(false)
  }

}
