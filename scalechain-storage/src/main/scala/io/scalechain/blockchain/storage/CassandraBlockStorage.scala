package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.index.{BlockDatabase, CassandraDatabase}
import org.slf4j.LoggerFactory


/** Store block headers, block transactions, the best block hash.
  */
class CassandraBlockStorage(directoryPath : File) extends BlockStorage {
  private val logger = LoggerFactory.getLogger(classOf[CassandraBlockStorage])

  protected[storage] val blockMetadataTable = new CassandraDatabase(directoryPath, "block_metadata")

  // Block info including block header,
  protected[storage] val blockMetadata = new BlockDatabase( blockMetadataTable )

  // The serialized blocks are stored on this table.
  // Key : Block header hash
  // Value : Serialized block
  protected[storage] val blocksTable = new CassandraDatabase(directoryPath, "blocks")

  // The serialized transactions are stored on this table.
  // Key : Transaction Hash
  // Value : Serialized transaction
  protected[storage] val transactionsTable = new CassandraDatabase(directoryPath, "transactions")


  protected[storage] def blockDatabase() = {
    blockMetadata
  }

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
      val blockInfo: Option[BlockInfo] = blockDatabase.getBlockInfo(blockHash)
      var isNewBlock = false

      if (blockInfo.isDefined) {
        // case 1 : block info was found
        if (blocksTable.get(blockHash.value).isEmpty) {
          // case 1.1 : The block was not found on the blocks table.
          putBlockToCassandra(blockHash, block)

          checkBestBlockHash(blockHash, blockInfo.get.height)

          //logger.info("The block data was updated. block hash : {}", blockHash)
        } else {
          // case 1.2 block info with a block locator was found
          // The block already exists. Do not put it once more.
          logger.warn("The block already exists. block hash : {}", blockHash)
        }
      } else {
        // case 2 : no block info was found.
        // get the height of the previous block, to calculate the height of the given block.
        val prevBlockHeightOption: Option[Int] = getBlockHeight(Hash(block.header.hashPrevBlock.value))

        if (prevBlockHeightOption.isDefined) {
          // case 2.1 : no block info was found, previous block header exists.

          val blockHeight = prevBlockHeightOption.get + 1
          val blockInfo = BlockInfo(
            height = blockHeight,
            transactionCount = block.transactions.size,
            // BUGBUG : Need to use enumeration
            status = 0,
            blockHeader = block.header,
            None
          )

          blockDatabase.putBlockInfo(blockHash, blockInfo)

          putBlockToCassandra(blockHash, block)

          checkBestBlockHash(blockHash, blockHeight)

          isNewBlock = true
          //logger.info("The new block was put. block hash : {}", blockHash)
        } else {
          // case 2.2 : no block info was found, previous block header does not exists.
          logger.warn("An orphan block was discarded while saving a block. block hash : {}", block.header)
        }
      }

      isNewBlock
    }
  }

  protected[storage] def putBlockToCassandra(blockHash : Hash, block : Block) : Unit = {
    for (transaction <- block.transactions) {
      // case 1.1 and case 2.1 has newly stored transactions.
      transactionsTable.put(
        transaction.hash.value,
        TransactionCodec.serialize(transaction))
    }

    blocksTable.put( blockHash.value, BlockCodec.serialize(block))
  }


  def getTransaction(transactionHash : Hash) : Option[Transaction] = {
    this.synchronized {
      val serializedTransactionOption = transactionsTable.get(transactionHash.value)
      serializedTransactionOption.map( TransactionCodec.parse(_) )
    }
  }

  /** Remove a transaction from the block storage.
    *
    * We need to remove a transaction that are stored in a block which is not in the best block chain any more.
    *
    * @param transactionHash The hash of the transaction to remove from the blockchain.
    */
  def removeTransaction(transactionHash : Hash) : Unit = {
    this.synchronized {
      transactionsTable.del(transactionHash.value)
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
      val blockInfoOption = blockDatabase.getBlockInfo(blockHash)
      if (blockInfoOption.isDefined) {
        // case 1 : The block info was found.
        val serializedBlockOption = blocksTable.get(blockHash.value)
        serializedBlockOption.map{ serializedBlock =>
          (blockInfoOption.get, BlockCodec.parse(serializedBlock) )
        }
      } else {
        // case 2 : The block info was not found
        //logger.info("getBlock - No block info found. block hash : {}", blockHash)
        None
      }
    }
  }


  def close() : Unit = {
    blockMetadata.close
    blocksTable.close
    transactionsTable.close
  }

}
