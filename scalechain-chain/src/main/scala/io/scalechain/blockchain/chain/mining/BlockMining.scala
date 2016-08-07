package io.scalechain.blockchain.chain.mining

import java.io.File

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.{TransactionPriorityQueue, TransactionMagnet, TransactionBuilder, TransactionPool}
import io.scalechain.blockchain.storage.index.DatabaseTablePrefixes._
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.{TransactionTimeIndex, TransactionPoolIndex, BlockStorage}
import io.scalechain.blockchain.storage.index.{TransactingRocksDatabase, KeyValueDatabase, RocksDatabase, TransactionDescriptorIndex}
import io.scalechain.blockchain.transaction.{CoinsView, CoinAmount, CoinAddress}
import io.scalechain.util.StopWatch
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.util.Random
import HashSupported._

trait TemporaryTransactionPoolIndex extends TransactionPoolIndex {
  protected override val PoolIndexPrefix = TEMP_TRANSACTION_POOL
}

trait TemporaryTransactionTimeIndex extends TransactionTimeIndex {
  protected override val TimeIndexPrefix = TEMP_TRANSACTION_TIME
}

class TemporaryCoinsView(coinsView : CoinsView) extends CoinsView {

  val tempTranasctionPoolIndex = new TemporaryTransactionPoolIndex {}
  val tempTranasctionTimeIndex = new TemporaryTransactionTimeIndex {}


  /** Return a transaction output specified by a give out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  def getTransactionOutput(outPoint : OutPoint)(implicit db : KeyValueDatabase) : TransactionOutput = {
    // Find from the temporary transaction pool index first, and then find from the transactions in a block.
    tempTranasctionPoolIndex.getTransactionFromPool(outPoint.transactionHash).map(_.transaction.outputs(outPoint.outputIndex)).getOrElse {
      // This is called by TransactionPriorityQueue, which already checked if the transaction is attachable.
      coinsView.getTransactionOutput(outPoint)
    }
  }
}


/**
  * Created by kangmo on 6/9/16.
  */
class BlockMining(txDescIndex : TransactionDescriptorIndex, transactionPool : TransactionPool, coinsView : CoinsView)(rocksDB : RocksDatabase) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[BlockMining]) )
  val watch = new StopWatch()
  /*
    /** Calculate the (encoded) difficulty bits that should be in the block header.
      *
      * @param prevBlockDesc The descriptor of the previous block. This method calculates the difficulty of the next block of the previous block.
      * @return
      */
    def calculateDifficulty(prevBlockDesc : BlockInfo) : Long = {
      if (prevBlockDesc.height == 0) { // The genesis block
        GenesisBlock.BLOCK.header.target
      } else {
        // BUGBUG : Make sure that the difficulty calculation is same to the one in the Bitcoin reference implementation.
        val currentBlockHeight = prevBlockDesc.height + 1
        if (currentBlockHeight % 2016 == 0) {
          // TODO : Calculate the new difficulty bits.
          assert(false)
          -1L
        } else {
          prevBlockDesc.blockHeader.target
        }
      }
    }
  */

  /** Get the template for creating a block containing a list of transactions.
    *
    * @return The block template which has a sorted list of transactions to include into a block.
    */
  def getBlockTemplate(coinbaseData : CoinbaseData, minerAddress : CoinAddress, maxBlockSize : Int) : BlockTemplate = {
    // TODO : P1 - Use difficulty bits
    //val difficultyBits = getDifficulty()
    val difficultyBits = 10


    val bytesPerTransaction = 256
    val estimatedTransactionCount = maxBlockSize / bytesPerTransaction

    watch.start("candidateTransactions")

    val candidateTransactions : List[(Hash, Transaction)] = transactionPool.getOldestTransactions(estimatedTransactionCount)(rocksDB)

    watch.stop("candidateTransactions")


    watch.start("validTransactions")

    var candidateTxCount = 0
    var validTxCount = 0
    val validTransactions : List[Transaction] = candidateTransactions.filter{
      // Because we are concurrently putting transactions into the pool while putting blocks,
      // There can be some transactions in the pool as well as on txDescIndex, where only transactions in a block is stored.
      // Skip all transactions that has the transaction descriptor.
      case (txHash, transaction) => {
        candidateTxCount += 1
        if ( txDescIndex.getTransactionDescriptor(txHash)(rocksDB).isEmpty ) {
          true
        } else {
          // Remove transactions from the pool if it is in a block as well.
          //transactionPool.removeTransactionFromPool(txHash)(rocksDB)
          false
        }

      }
    }.map {
      case (txHash, transaction) => {
        validTxCount += 1
        transaction
      }
    }


    // Remove transactions from the pool if it is in a block as well.
    candidateTransactions.filter{
      // Because we are concurrently putting transactions into the pool while putting blocks,
      // There can be some transactions in the pool as well as on txDescIndex, where only transactions in a block is stored.
      // Skip all transactions that has the transaction descriptor.
      case (txHash, transaction) =>
        // If the transaction descriptor exists, it means the transaction is in a block.
        txDescIndex.getTransactionDescriptor(txHash)(rocksDB).isDefined
    }.foreach { case (txHash, transaction) => {
        logger.info(s"A Transaction in a block removed from pool. Hash : ${txHash} ")
        transactionPool.removeTransactionFromPool(txHash)(rocksDB)
      }
    }


    val generationTransaction =
      TransactionBuilder.newGenerationTransaction(coinbaseData, minerAddress)
    watch.stop("validTransactions")


    watch.start("selectTx")
    // Select transactions by priority and fee. Also, sort them.
    val (txCount, sortedTransactions) = selectTransactions(generationTransaction, validTransactions, maxBlockSize)
    watch.stop("selectTx")

    val firstCandidateHash = if (candidateTransactions.isEmpty) None else Some(candidateTransactions.head._1)
    val newCandidates = transactionPool.getOldestTransactions(1)(rocksDB)
    val newFirstCandidateHash = if (newCandidates.isEmpty) None else Some(newCandidates.head._1)

    logger.info(s"Coin Miner stats : ${watch.toString}, First Candidate Tx : ${firstCandidateHash}, New First Candidate Tx : ${newFirstCandidateHash}, Candidate Tx Count : ${candidateTxCount}, Valid Tx Count : ${validTxCount}, Attachable Tx Count : ${txCount}")
    new BlockTemplate(difficultyBits, sortedTransactions)
  }


  /** Select transactions to include into a block.
    *
    *  Order transactions by dependency and by fee(in descending order).
    *  List N transactions based on the priority and fee so that the serialzied size of block
    *  does not exceed the max size. (ex> 1MB)
    *
    *  <Called by>
    *  When a miner tries to create a block, we have to create a block template first.
    *  The block template has the transactions to keep in the block.
    *  In the block template, it has all fields set except the nonce and the timestamp.
    *
    *  The first criteria for ordering transactions in a block is the transaction dependency.
    *
    *  Why is ordering transactions in a block based on dependency is necessary?
    *    When blocks are reorganized, transactions in the block are detached the reverse order of the transactions stored in a block.
    *    Also, they are attached in the same order of the transactions stored in a block.
    *    The order of transactions in a block should be based on the dependency, otherwise, an outpoint in an input of a transaction may point to a non-existent transaction by the time it is attached.    *
    *
    *
    *  How?
    *    1. Create a priority queue that has complete(= all required transactions exist) transactions.
    *    2. The priority is based on the transaction fee, for now. In the future, we need to improve the priority to consider the amount of coin to transfer.
    *    3. Prepare a temporary transaction pool. The pool will be used to look up dependent transactions while trying to attach transactions.
    *    4. Try to attach each transaction in the input list depending on transactions on the temporary transaction pool instead of the transaction pool in Blockchain. (We should not actually attach the transaction, but just 'try to' attach the transaction without changing the "spent" in-point of UTXO.)
    *    5. For all complete transactions that can be attached, move from the input list to the priority queue.
    *    6. If there is any transaction in the priority queue, pick the best transaction with the highest priority into the temporary transaction pool, and Go to step 4. Otherwise, stop iteration.
    *
    * @param transactions The candidate transactions
    * @param maxBlockSize The maximum block size. The serialized block size including the block header and transactions should not exceed the size.
    * @return The count and list of transactions to put into a block.
    */
  protected[chain] def selectTransactions(generationTransaction:Transaction, transactions : List[Transaction], maxBlockSize : Int) : (Int, List[Transaction]) = {

//    val candidateTransactions = new ListBuffer[Transaction]()
//    candidateTransactions ++= transactions
    val selectedTransactions = new ListBuffer[Transaction]()

    val BLOCK_HEADER_SIZE = 80
    val MAX_TRANSACTION_LENGTH_SIZE = 9 // The max size of variable int encoding.
    var serializedBlockSize = BLOCK_HEADER_SIZE + MAX_TRANSACTION_LENGTH_SIZE


    serializedBlockSize += TransactionCodec.serialize(generationTransaction).length
    selectedTransactions.append(generationTransaction)


    // Create a temporary database just for checking if transactions can be attached.
    // We should never commit the tempDB.
    implicit val tempDB = new TransactingRocksDatabase(rocksDB)
    tempDB.beginTransaction()


    // Remove all transactions in the pool


    // For all attachable transactions, attach them, and move to the priority queue.
    //    val tempPoolDbPath = new File(s"target/temp-tx-pool-for-mining-${Random.nextLong}")
    //    tempPoolDbPath.mkdir
    val tempCoinsView = new TemporaryCoinsView(coinsView)

    try {
      // The TemporaryCoinsView with additional transactions in the temporary transaction pool.
      // TemporaryCoinsView returns coins in the transaction pool of the coinsView, which may not be included in tempTranasctionPoolIndex,
      // But this should be fine, because we are checking if a transaction can be attached without including the transaction pool of the coinsView.
      val txQueue = new TransactionPriorityQueue(tempCoinsView)

      val txMagnet = new TransactionMagnet(txDescIndex, tempCoinsView.tempTranasctionPoolIndex, tempCoinsView.tempTranasctionTimeIndex )

      var newlySelectedTransaction : Option[Transaction] = None
      /*
      do {
        val iter = candidateTransactions.iterator
        var consequentNonAttachableTx = 0
        // If only some of transaction is attachable, (ex> 1 out of 4000), the loop takes too long.
        // So get out of the loop if N consecutive transactions are not attachable.
        // BUGBUG : Need to Use a random value instead of 8
        while( consequentNonAttachableTx < 3 && iter.hasNext) {
          val tx: Transaction = iter.next
          val txHash = tx.hash
          // Test if it can be atached.
          val isTxAttachable = try {
            txMagnet.attachTransaction(txHash, tx, checkOnly = true)
            true
          } catch {
            case e: ChainException => {
              false
            } // The transaction can't be attached.
          }

          if (isTxAttachable) {
            //println(s"attachable : ${txHash}")
            // move the the transaction queue
            candidateTransactions -= tx
            txQueue.enqueue(tx)
            consequentNonAttachableTx = 0
          } else {
            consequentNonAttachableTx += 1
          }
        }

        newlySelectedTransaction = txQueue.dequeue()

        //        println(s"newlySelectedTransaction ${newlySelectedTransaction}")

        if (newlySelectedTransaction.isDefined) {
          val newTx = newlySelectedTransaction.get
          serializedBlockSize += TransactionCodec.serialize(newTx).length
          if (serializedBlockSize <= maxBlockSize) {
            // Attach the transaction
            txMagnet.attachTransaction(newTx.hash, newTx, checkOnly = false)
            selectedTransactions += newTx
          }
        }

      } while(newlySelectedTransaction.isDefined && (serializedBlockSize <= maxBlockSize) )
      // Caution : serializedBlockSize is greater than the actual block size
      */

      var txCount = 0
      val iter = transactions.iterator

      while( iter.hasNext && (serializedBlockSize <= maxBlockSize) ) {
        val tx: Transaction = iter.next
        val txHash = tx.hash

        // Test if it can be atached.
        try {
          txMagnet.attachTransaction(txHash, tx, checkOnly = true)
          serializedBlockSize += TransactionCodec.serialize(tx).length

          if (serializedBlockSize <= maxBlockSize) {
            // Attach the transaction
            txMagnet.attachTransaction(txHash, tx, checkOnly = false)
            selectedTransactions += tx
            txCount += 1
          }

        } catch {
          case e: ChainException => {
          } // The transaction can't be attached.
        }

      }

      (txCount, selectedTransactions.toList)

    } finally {
      tempDB.abortTransaction
    }
  }
}
