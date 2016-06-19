package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.index.{BlockDatabaseForRecordStorage, RocksDatabase, BlockDatabase, CassandraDatabase}
import io.scalechain.crypto.HashEstimation
import org.slf4j.LoggerFactory

// A version Using CassandraBlockStorage
object CassandraBlockStorage {
  val MAX_FILE_SIZE = 1024 * 1024 * 100
  //val MAX_FILE_SIZE = 1024 * 1024 * 1


  var theBlockStorage : CassandraBlockStorage = null

  def create(directoryPath : File, cassandraAddress : String, cassandraPort : Int) : BlockStorage = {
    assert(theBlockStorage == null)
    theBlockStorage = new CassandraBlockStorage(directoryPath, cassandraAddress, cassandraPort)

    theBlockStorage
  }

  /** Get the block storage. This actor is a singleton, used by transaction validator.
    *
    * @return The block storage.
    */
  def get() : BlockStorage = {
    assert(theBlockStorage != null)
    theBlockStorage
  }

}


/** Store block headers, block transactions, the best block hash.
  */
class CassandraBlockStorage(directoryPath : File, cassandraAddress : String, cassandraPort : Int) extends BlockStorage {
  private val logger = LoggerFactory.getLogger(classOf[CassandraBlockStorage])

  private val rocksDatabasePath = new File( directoryPath, "rocksdb")
  rocksDatabasePath.mkdir()

  // Implemenent the KeyValueDatabase declared in BlockStorage trait.
  protected[storage] val keyValueDB = new RocksDatabase( rocksDatabasePath )

  // Implemenent the BlockDatabase declared in BlockStorage trait.
  // Block info including block header,
  protected[storage] val blockDatabase = new BlockDatabase( keyValueDB )


  // The serialized blocks are stored on this table.
  // Key : Block header hash
  // Value : Serialized block
  protected[storage] val blocksTable = new CassandraDatabase(cassandraAddress, cassandraPort, "blocks")

  // The serialized transactions are stored on this table.
  // Key : Transaction Hash
  // Value : Serialized transaction
  protected[storage] val transactionsTable = new CassandraDatabase(cassandraAddress, cassandraPort, "transactions")


  /** Store a block.
    *
    * @param blockHash the hash of the header of the block to store.
    * @param block the block to store.
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

          //logger.info("The block data was updated. block hash : {}", blockHash)
        } else {
          // case 1.2 block info with a block locator was found
          // The block already exists. Do not put it once more.
          // This can happen when the mining node already had put the block, but other nodes tried to put it once more.
          // (Because a cassandra cluster is shared by all nodes )
          logger.warn("The block already exists. block hash : {}", blockHash)
        }
      } else {
        // case 2 : no block info was found.
        // get the height of the previous block, to calculate the height of the given block.
        val prevBlockInfoOption: Option[BlockInfo] = getBlockInfo(Hash(block.header.hashPrevBlock.value))

        // Either the previous block should exist or the block should be the genesis block.
        if (prevBlockInfoOption.isDefined || block.header.hashPrevBlock.isAllZero()) {
          // case 2.1 : no block info was found, previous block header exists.

          val blockInfo = BlockInfoFactory.create(
            prevBlockInfoOption,
            block.header,
            blockHash,
            block.transactions.length, // transaction count
            None // block locator
          )

          blockDatabase.putBlockInfo(blockHash, blockInfo)

          putBlockToCassandra(blockHash, block)

          val blockHeight = blockInfo.height

          isNewBlock = true
          //logger.info("The new block was put. block hash : {}", blockHash)
        } else {
          // case 2.2 : no block info was found, previous block header does not exists.

          // Actually the code execution should never come to here, because we have checked if the block is an orphan block
          // before invoking putBlock method.

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

  /**
    * Get a transaction stored in a block or get it from transaction pool.
    *
    * TODO : Add test case.
    * @param transactionHash
    * @return
    */
  def getTransaction(transactionHash : Hash) : Option[Transaction] = {
    this.synchronized {
      val serializedTransactionOption = transactionsTable.get(transactionHash.value)
      if (serializedTransactionOption.isDefined) {
        serializedTransactionOption.map(TransactionCodec.parse(_))
      } else {
        getTransactionFromPool(transactionHash)
      }
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
    blockDatabase.close
    blocksTable.close
    transactionsTable.close
  }

}
