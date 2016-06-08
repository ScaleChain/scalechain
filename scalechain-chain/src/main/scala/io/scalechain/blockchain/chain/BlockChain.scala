package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.codec.{TransactionCodec, BlockHeaderCodec}
import io.scalechain.blockchain.{ChainException, ErrorCode, GeneralException}
import io.scalechain.blockchain.chain.mining.BlockTemplate
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.{BlockInfoFactory, BlockStorage, Storage, DiskBlockStorage, GenesisBlock}
import io.scalechain.blockchain.storage

import io.scalechain.blockchain.transaction._
import io.scalechain.util.Utils
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.TailCalls.TailRec


object Blockchain {
  var theBlockchain : Blockchain = null
  def create(storage : BlockStorage) = {
    theBlockchain = new Blockchain(storage)

    // Load any in memory structur required by the Blockchain class from the on-disk storage.
    new BlockchainLoader(theBlockchain, storage).load()
    theBlockchain
  }
  def get() = {
    assert( theBlockchain != null)
    theBlockchain
  }
}


class BlockchainLoader(chain:Blockchain, storage : BlockStorage) {

  def load() : Unit = {
    val bestBlockHashOption = storage.getBestBlockHash()
    if (bestBlockHashOption.isDefined) {
      // Set the best block descriptor.
      chain.theBestBlock  = storage.getBlockInfo(bestBlockHashOption.get).get
    } else {
      // We don't have the best block hash yet.
      // This means that we did not put the genesis block yet.
      // On the CLI layer, while initializing all layers, the genesis block will be put, so we do nothing here.
    }
  }
}

/** Maintains the best blockchain, whose chain work is the biggest one.
  *
  * The block metadata is kept in a tree data structure on-disk.
  * The actual block data is also kept on-disk.
  *
  * [ Overview ]
  *
  * The chain work for a block is the total number of hash calculations from block 0 to the current best block.
  *
  * For example, if we calculated hashes 10, 20, 15 times for three blocks B0, B1, and B2, the chain work is 45(10+20+15).
  *
  *   B0(10) → B1(10+20) → B2(10+20+15) : The best chain.
  *
  * Based on the total chain work of the new block, we decide the best blockchain.
  * For example, if we found a block B2' whose chain work(50) is greater than the current maxium(45),
  * we will keep B2' as the best block and update the best blockchain.
  *
  *   B0(10) → B1(10+20) → B2'(10+20+20) : The best chain.
  *                      ↘ B2(10+20+15) : This is a fork.
  *
  * When a new block B3 is added to the blockchain, we will add it on top of the best blockchain.
  *
  *   B0 → B1 → B2' → B3 : The best chain.
  *           ↘ B2
  *
  * Only transactions in the best blockchain remain effective.
  * Because B2 remains in a fork, all transactions in B2 are migrated to the disk-pool, except ones that are included in B3.
  *
  * The disk-pool is where transactions that are not in any block of the best blockchain are stored.
  * ( Bitcoin core stores transactions in memory using mempool, but ScaleChain stores transactions on-disk using disk-pool ;-). )
  * TransactionDescriptor can either store record location of the transaction if the transaction was written as part of a block on disk.
  * Otherwise, TransactionDescriptor can stores a serialized transaction, and TransactionDescriptor itself is stored as a value of RocksDB keyed by the transaction hash.
  *
  * Of course, block a reorganization can invalidate more than two blocks at once.
  *
  * Time T :
  *   B0(10) → B1(30) → B2(45) : The best chain.
  *
  * Time T+1 : Add B1' (chain work = 35)
  *   B0(10) → B1(30) → B2(45) : The best chain.
  *          ↘ B1'(35)
  *
  * Time T+2 : Add B2' (chain work = 55)
  *   B0(10) → B1(30) → B2(45)
  *          ↘ B1'(35) -> B2'(55) : The best chain.
  *
  * In this case all transactions in B1, B2 but not in B1' and B2' are moved to the disk-pool so that they can be added to
  * the block chain later when a new block is created.
  *
  */
class Blockchain(storage : BlockStorage) extends BlockchainView with ChainConstraints {
  private val logger = LoggerFactory.getLogger(classOf[Blockchain])

  var chainEventListener : Option[ChainEventListener] = None

  /** Set an event listener of the blockchain.
    *
    * @param listener The listener that wants to be notified for new blocks, invalidated blocks, and transactions comes into and goes out from the transaction pool.
    */
  def setEventListener( listener : ChainEventListener ): Unit = {
    chainEventListener = Some(listener)
  }

  /** The descriptor of the best block.
    * This value is updated whenever a new best block is found.
    * We also have to check if we need to do block reorganization whenever this field is updated.
    */
  var theBestBlock : BlockInfo = null

  /**
    * Put the best block hash into on-disk storage, as well as the in-memory best block info.
    *
    * @param blockHash
    * @param blockInfo
    */
  protected[chain] def setBestBlock(blockHash : Hash, blockInfo : BlockInfo) : Unit = {
    theBestBlock = blockInfo
    storage.putBestBlockHash(blockHash)
  }

  /** Put a block onto the blockchain.
    *
    * (1) During initialization, we call putBlock for each block we received until now.
    * (2) During IBD(Initial Block Download), we call putBlock for blocks we downloaded.
    * (3) When a new block was received from a peer.
    *
    * Caller of this method should check if the bestBlock was changed.
    * If changed, we need to update the best block on the storage layer.
    *
    * TODO : Need to check the merkle root hash in the block.
    *
    * @param block The block to put into the blockchain.
    */
  def putBlock(blockHash : Hash, block:Block) : Unit = {
    // TODO : BUGBUG : Need to think about RocksDB transactions.

    synchronized {
      if (storage.hasBlock(blockHash)) {
        logger.info(s"Duplicate block was ignored. Block hash : ${blockHash}")
      } else {

        // Case 1. If it is the genesis block, set the genesis block as the current best block.
        if (block.header.hashPrevBlock.isAllZero()) {
          assert(theBestBlock == null)
          storage.putBlock(block)
          storage.putBlockHashByHeight(0, blockHash)

          setBestBlock(blockHash, storage.getBlockInfo(blockHash).get )

          chainEventListener.map(_.onAttachBlock(ChainBlock( height = 0, block)))
        } else { // Case 2. Not a genesis block.
          assert(theBestBlock != null)

          // Step 2.1 : Get the block descriptor of the previous block.
          val prevBlockDesc: Option[BlockInfo] = storage.getBlockInfo(block.header.hashPrevBlock)
          // We already checked if the parent block exists.
          assert(prevBlockDesc.isDefined)

          // Case 2.A : The previous block of the new block is the current best block.
          if (prevBlockDesc.get.blockHeader.hash.value == theBestBlock.blockHeader.hash.value) {
            // Step 2.A.1 : Update the best block
            storage.putBlock(block)
            val blockInfo = storage.getBlockInfo(blockHash).get
            storage.putBlockHashByHeight(blockInfo.height, blockHash)

            setBestBlock( blockHash, blockInfo )

            // TODO : Update best block in wallet (so we can detect restored wallets)

            // Step 2.A.2 : Remove transactions in the block from the disk-pool.
            block.transactions.foreach { transaction =>
              storage.delTransactionFromPool(transaction.hash)
            }

            chainEventListener.map(_.onAttachBlock(ChainBlock(height = blockInfo.height, block)))

            logger.info(s"Successfully have put the block in the best blockchain.\n Hash : ${blockHash}")

          } else { // Case 2.B : The previous block of the new block is NOT the current best block.
            storage.putBlock(block)
            val blockInfo = storage.getBlockInfo(blockHash).get
            theBestBlock = blockInfo

            // Step 3.B.1 : See if the chain work of the new block is greater than the best one.
            if (blockInfo.chainWork > theBestBlock.chainWork) {
              logger.warn("Block reorganization started.")

              // Step 3.B.2 : Reorganize the blocks.
              // transaction handling, orphan block handling is done in this method.
              reorganize(originalBestBlock = theBestBlock, newBestBlock = blockInfo)

              // Step 3.B.3 : Update the best block
              setBestBlock(blockHash, blockInfo)

              // TODO : Update best block in wallet (so we can detect restored wallets)
            }
          }
        }
      }
    }
  }

  /** Put a transaction we received from peers into the disk-pool.
    *
    * @param transaction The transaction to put into the disk-pool.
    */
  def putTransaction(transaction : Transaction) : Unit = {
    synchronized {
      // TODO : BUGBUG : Need to start a RocksDB transaction.
      try {
        val txHash = transaction.hash
        addTransactionToDiskPool(txHash, transaction)
        // TODO : BUGBUG : Need to commit the RocksDB transaction.
      } finally {
        // TODO : BUGBUG : Need to rollback the RocksDB transaction if any exception raised.
        // Only some of inputs might be connected. We need to revert the connection if any error happens.
      }
    }
  }


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

  /** Get the template for creating a block containing a list of transactions.
    *
    * @return The block template which has a sorted list of transactions to include into a block.
    */
  def getBlockTemplate(coinbaseData : CoinbaseData, minerAddress : CoinAddress, maxBlockSize : Int) : BlockTemplate = {
    // TODO : P1 - Use difficulty bits
    //val difficultyBits = getDifficulty()
    val difficultyBits = 10

    val validTransactions : List[Transaction] = storage.getTransactionsFromPool().map {
      case (txHash, transaction) => transaction
    }

    val generationTranasction =
      TransactionBuilder.newBuilder(this)
        .addGenerationInput(coinbaseData)
        .addOutput(CoinAmount(50), minerAddress)
        .build()

    // Select transactions by priority and fee. Also, sort them.
    val sortedTransactions = selectTransactions(generationTranasction, validTransactions, maxBlockSize)

    new BlockTemplate(difficultyBits, sortedTransactions)
  }


  /** Select transactions to include into a block.
    *
    *  Order transactions by fee in descending order.
    *  List N transactions based on the priority and fee so that the serialzied size of block
    *  does not exceed the max size. (ex> 1MB)
    *
    *  <Called by>
    *  When a miner tries to create a block, we have to create a block template first.
    *  The block template has the transactions to keep in the block.
    *  In the block template, it has all fields set except the nonce and the timestamp.
    *
    * @param transactions The candidate transactions
    * @param maxBlockSize The maximum block size. The serialized block size including the block header and transactions should not exceed the size.
    * @return The transactions to put into a block.
    */
  protected def selectTransactions(generationTransaction:Transaction, transactions : List[Transaction], maxBlockSize : Int) : List[Transaction] = {
    val selectedTransactions = new ListBuffer[Transaction]()
    // Step 1 : TODO : Select high priority transactions

    // Step 2 : TODO : Sort transactions by fee in descending order.

    // Step 3 : TODO : Choose transactions until we fill up the max block size.

    // Step 4 : TODO : Sort transactions based on a criteria to store into a block.

    // TODO : Need to check the sort order of transactions in a block.
    // TODO : Need to calculate the size of the block header on the fly instead of using the hard coded value 80.
    val BLOCK_HEADER_SIZE = 80
    val MAX_TRANSACTION_LENGTH_SIZE = 9 // The max size of variable int encoding.
    var serializedBlockSize = BLOCK_HEADER_SIZE + MAX_TRANSACTION_LENGTH_SIZE

    serializedBlockSize += TransactionCodec.serialize(generationTransaction).length
    selectedTransactions.append(generationTransaction)

    transactions foreach { tx =>
      serializedBlockSize += TransactionCodec.serialize(tx).length
      if (serializedBlockSize <= maxBlockSize) {
        selectedTransactions.append(tx)
      }
    }

    selectedTransactions.toList
  }

  /**
    * Mark an output spent by the given in-point.
    *
    * @param outPoint The out-point that points to the output to mark.
    * @param inPoint The in-point that points to a transaction input that spends to output.
    */
  protected[chain] def markOutputSpent(outPoint : OutPoint, inPoint : InPoint): Unit = {
    val txDesc = storage.getTransactionDescriptor(outPoint.transactionHash).getOrElse {
      val message = s"An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) points to a transaction that does not exist yet."
      logger.warn(message)
      throw new ChainException(ErrorCode.ParentTransactionNotFound, message)
    }

    // TODO : BUGBUG : indexing into a list is slow. Optimize the code.
    if ( outPoint.outputIndex < 0 || txDesc.outputsSpentBy.length <= outPoint.outputIndex ) {
      // TODO : Add DoS score. The outpoint in a transaction input was invalid.
      val message = s"An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) has invalid transaction output index."
      logger.warn(message)
      throw new ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    if( txDesc.outputsSpentBy(outPoint.outputIndex).isDefined ) { // The transaction output was already spent.
      val message = s"An output pointed by an out-point(${outPoint}) has already been spent. The in-point(${inPoint}) tried to spend it again."
      logger.warn(message)
      throw new ChainException(ErrorCode.TransactionOutputAlreadySpent, message)
    }

    storage.putTransactionDescriptor(
      outPoint.transactionHash,
      txDesc.copy(
        // Mark the output spent by the in-point.
        outputsSpentBy = txDesc.outputsSpentBy.updated(outPoint.outputIndex, Some(inPoint))
      )
    )
  }

  /**
    * Mark an output unspent. The output should have been marked as spent by the given in-point.
    *
    * @param outPoint The out-point that points to the output to mark.
    * @param inPoint The in-point that points to a transaction input that should have spent the output.
    */
  protected[chain] def markOutputUnspent(outPoint : OutPoint, inPoint : InPoint): Unit = {
    val Some(txDesc) = storage.getTransactionDescriptor(outPoint.transactionHash)
    // TODO : BUGBUG : indexing into a list is slow. Optimize the code.
    if ( outPoint.outputIndex < 0 || txDesc.outputsSpentBy.length <= outPoint.outputIndex ) {
      // TODO : Add DoS score. The outpoint in a transaction input was invalid.
      val message = s"An output pointed by an out-point(${outPoint}) has invalid transaction output index. The output should have been spent by ${inPoint}"
      logger.warn(message)
      throw new ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    val SpendingInPointOption = txDesc.outputsSpentBy(outPoint.outputIndex)
    // The output pointed by the out-point should have been spent by the transaction input poined by the given in-point.
    assert( SpendingInPointOption.isDefined )

    if( SpendingInPointOption.get != inPoint ) { // The transaction output was NOT spent by the transaction input poined by the given in-point.
      val message = s"An output pointed by an out-point(${outPoint}) was not spent by the expected transaction input pointed by the in-point(${inPoint})."
      logger.warn(message)
      throw new ChainException(ErrorCode.TransactionOutputSpentByUnexpectedInput, message)
    }

    storage.putTransactionDescriptor(
      outPoint.transactionHash,
      txDesc.copy(
        // Mark the output unspent.
        outputsSpentBy = txDesc.outputsSpentBy.updated(outPoint.outputIndex, None)
      )
    )
  }

  /**
    * Mark all outputs of the given transaction unspent.
    * Called when a new transaction is attached to the best blockchain.
    *
    * @param txHash The hash of the transaction.
    */
  protected[chain] def markAllOutputsUnspent(txHash : Hash): Unit = {
    val Some(txDesc) = storage.getTransactionDescriptor(txHash)
    storage.putTransactionDescriptor(
      txHash,
      txDesc.copy(
        // Mark all outputs unspent.
        outputsSpentBy = List.fill(txDesc.outputsSpentBy.length)(None)
      )
    )
  }


  /**
    * Detach the transaction input from the best blockchain.
    * The output spent by the transaction input is marked as unspent.
    *
    * @param inPoint The in-point that points to the input to attach.
    * @param transactionInput The transaction input to attach.
    */
  protected[chain] def detachTransactionInput(inPoint : InPoint, transactionInput : TransactionInput) : Unit = {
    // Make sure that the transaction input is not a coinbase input. detachBlock already checked if the input was NOT coinbase.
    assert(!transactionInput.isCoinBaseInput())

    markOutputUnspent(transactionInput.getOutPoint(), inPoint)
  }

  /**
    * Detach each of transction inputs. Mark outputs spent by the transaction inputs unspent.
    *
    * @param transactionHash The hash of the tranasction that has the inputs.
    * @param transaction The transaction that has the inputs.
    */
  protected[chain] def detachTransactionInputs(transactionHash : Hash, transaction : Transaction) : Unit = {
    var inputIndex = -1
    transaction.inputs foreach { transactionInput : TransactionInput =>
      inputIndex += 1

      // Make sure that the transaction input is not a coinbase input. detachBlock already checked if the input was NOT coinbase.
      assert(!transactionInput.isCoinBaseInput())

      detachTransactionInput(InPoint(transactionHash, inputIndex), transactionInput)
    }
  }

  /**
    * Add a transaction to disk pool.
    *
    * Assumption : The transaction was pointing to a transaction record location, which points to a transaction written while the block was put into disk.
    *
    * @param txHash The hash of the transaction to add.
    * @param transaction The transaction to add to the disk-pool.
    *
    * @return true if the transaction was valid with all inputs connected. false otherwise. (ex> orphan transactions return false )
    */

  def addTransactionToDiskPool(txHash : Hash, transaction : Transaction) : Unit = {
    // Step 01 : Check if the transaction exists in the disk-pool.
    if ( storage.getTransactionFromPool(txHash).isDefined ) {
      logger.info(s"A duplicate transaction in the pool was discarded. Hash : ${txHash}")
    } else {
      // Step 02 : Check if the transaction exists in a block in the best blockchain.
      val txDescOption = storage.getTransactionDescriptor(txHash)
      if (txDescOption.isDefined && txDescOption.get.transactionLocatorOption.isDefined ) {
        logger.info(s"A duplicate transaction in on a block was discarded. Hash : ${txHash}")
      } else {
        // Step 03 : CheckTransaction - check values in the transaction.

        // Step 04 : IsCoinBase - the transaction should not be a coinbase transaction. No coinbase transaction is put into the disk-pool.

        // Step 05 : GetSerializeSize - Check the serialized size

        // Step 06 : GetSigOpCount - Check the script operation count.

        // Step 07 : IsStandard - Check if the transaction is a standard one.

        // Step 08 : Check for double-spends with existing transactions,
        attachTransactionInputs(txHash, transaction)

        // Step 09 : Check the transaction fee.

        // Step 10 : Add to the disk-pool
        storage.putTransactionToPool(txHash, transaction)

        // Step 11 : Add the transaction descriptor without the transactionLocatorOption.
        //          The transaction descriptor is necessary to mark the outputs of the transaction either spent or unspent.
        val txDesc =
          TransactionDescriptor(
            transactionLocatorOption = None,
            List.fill(transaction.outputs.length)(None) )

        storage.putTransactionDescriptor(txHash, txDesc)

        // Step 12 : Notify event listeners that a new transaction was added.
        chainEventListener.map(_.onNewTransaction(transaction))

        logger.info(s"A new transaction was put into pool. Hash : ${txHash}")
      }
    }
  }

  /**
    * Remove a transaction from the disk pool.
    *
    * @param txHash The hash of the transaction to remove.
    */
  protected[chain] def removeTransactionFromDiskPool(txHash : Hash) : Unit = {
    // Note : We should not touch the TransactionDescriptor.
    storage.delTransactionFromPool(txHash)

  }


  /**
    * Detach the transaction from the best blockchain.
    *
    * For outputs, all outputs spent by the transaction is marked as unspent.
    *
    * @param transaction The transaction to detach.
    */
  protected[chain] def detachTransaction(transaction : Transaction) : Unit = {
    val transactionHash = transaction.hash
    // Step 1 : Detach each transaction input
    detachTransactionInputs(transactionHash, transaction)

    // TODO : BUGBUG : P0 : Need to reset the transaction locator of the transaction descriptor.

    // Step 2 : Move the transaction into disk-pool.
    // TODO : Do we really need to reset the transaction record locator? How do we recover it when we attach the transaction again?
    addTransactionToDiskPool(transactionHash, transaction)
  }

  /**
    * Detach a block from the best blockchain.
    *
    * @param blockInfo The BlockInfo of the block to detach.
    * @param block The block to detach.
    */
  protected[chain] def detachBlock(blockInfo: BlockInfo, block : Block) : Unit = {
    block.transactions foreach { transaction : Transaction =>
      if (transaction.inputs(0).isCoinBaseInput()) {
        // The coin base input does not spend any previous output. Do nothing.
      } else {
        detachTransaction(transaction)
      }
    }

    // For each transaction in the block, sync with wallet.
    chainEventListener.map( _.onDetachBlock( ChainBlock(blockInfo.height, block ) ) )
  }

  /**
    * Detach blocks from the best blockchain. Recent blocks are detached first. (Detach order : Newest -> Oldest )
    *
    * @param beforeFirst Blocks after this block are detached from the best blockchain.
    * @param last The last block in the best blockchain.
    */
  @tailrec
  protected[chain] final def detachBlocksAfter(beforeFirst : BlockInfo, last : BlockInfo, lastBlock : Block) : Unit = {
    assert(beforeFirst.height <= last.height)

    if (beforeFirst == last) {
      // The base case. Just unlink the next block hash of the before the first block to detach.
      storage.updateNextBlockHash(beforeFirst.blockHeader.hash, None)
    } else {
      detachBlock(last, lastBlock)
      // Unlink the next block hash.
      storage.updateNextBlockHash(last.blockHeader.hash, None)

      // Get the block before the last one, continue iteration.
      val Some((beforeLastInfo, beforeLastBlock)) = storage.getBlock(last.blockHeader.hashPrevBlock)
      detachBlocksAfter(beforeFirst, beforeLastInfo, beforeLastBlock)
    }
  }


  /**
    * The UTXO pointed by the transaction input is marked as spent by the in-point.
    *
    * @param inPoint The in-point that points to the input to attach.
    * @param transactionInput The transaction input to attach.
    */
  protected[chain] def attachTransactionInput(inPoint : InPoint, transactionInput : TransactionInput) : Unit = {
    // Make sure that the transaction input is not a coinbase input. attachBlock already checked if the input was NOT coinbase.
    assert(!transactionInput.isCoinBaseInput())

    // TODO : Step 1. read CTxIndex from disk if not read yet.
    // TODO : Step 2. read the transaction that the outpoint points from disk if not read yet.
    // TODO : Step 3. Increase DoS score if an invalid output index was found in a transaction input.
    // TODO : Step 4. check coinbase maturity for outpoints spent by a transaction.
    // TODO : Step 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
    // TODO : Step 6. check value range of each input and sum of inputs.
    // TODO : Step 7. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output. check double spends.
    markOutputSpent(transactionInput.getOutPoint(), inPoint)
  }

  /** Attach the transaction inputs to the outputs spent by them.
    * Mark outputs spent by the transaction inputs.
    *
    * @param transactionHash The hash of the tranasction that has the inputs.
    * @param transaction The transaction that has the inputs.
    */
  protected[chain] def attachTransactionInputs(transactionHash : Hash, transaction : Transaction) : Unit = {
    var inputIndex = -1
    transaction.inputs foreach { transactionInput : TransactionInput =>
      // Make sure that the transaction input is not a coinbase input. attachBlock already checked if the input was NOT coinbase.
      assert(!transactionInput.isCoinBaseInput())
      inputIndex += 1

      attachTransactionInput(InPoint(transactionHash, inputIndex), transactionInput)
    }
  }

  /**
    * Attach the transaction into the best blockchain.
    *
    * For UTXOs, all outputs spent by the transaction is marked as spent by this transaction.
    *
    * @param transaction The transaction to attach.
    */
  protected[chain] def attachTransaction(transactionHash : Hash, transaction : Transaction) : Unit = {
    // Step 1 : Attach each transaction input
    attachTransactionInputs(transactionHash, transaction)

    // TODO : BUGBUG : P0 : Need to set the transaction locator of the transaction descriptor according to the location of the attached block.

    // TODO : Step 2 : check if the sum of input values is greater than or equal to the sum of outputs.
    // TODO : Step 3 : make sure if the fee is not negative.
    // TODO : Step 4 : check the minimum transaction fee for each transaction.

    // Step 5 : Remove the transaction from the disk pool.
    removeTransactionFromDiskPool(transactionHash)
  }


  /**
    * Attach a block to the best blockchain.
    *
    * @param block The block to attach
    */
  protected[chain] def attachBlock(blockInfo: BlockInfo, block : Block) : Unit = {
    // Check if the block is valid.
    BlockProcessor.validateBlock(block)

    block.transactions foreach { transaction : Transaction =>
      val transactionHash = transaction.hash

      if (transaction.inputs(0).isCoinBaseInput()) {
        // The coin base input does not spend any previous output. Do nothing.
      } else {
        attachTransaction(transactionHash, transaction)
      }

      // Add UTXO : set all outputs are unspent for the newly attached transaction.
      markAllOutputsUnspent(transactionHash)
    }

    // TODO : Check if the generation transaction's output amount is less than or equal to the reward + sum of fees for all transactions in the block.

    // For each transaction in the block, sync with wallet.
    chainEventListener.map( _.onAttachBlock( ChainBlock(blockInfo.height, block ) ) )
  }

  /**
    * collect BlockInfos from the last to the next block of the beforeFirst.
    * The collected BlockInfos are kept in blockInfos List in ascending order. (Order : Oldest -> Newest)
    *
    * @param blockInfos The list buffer to keep the block info.
    * @param beforeFirst Blocks after this block are collected.
    * @param last The last block to collect.
    */
  @tailrec
  protected[chain] final def collectBlockInfos(blockInfos : ListBuffer[BlockInfo], beforeFirst : BlockInfo, last : BlockInfo) : Unit = {
    // TODO : Need to check if our memory is enough by checking the gap of the height between beforeFirst and last
    if (beforeFirst == last) {
      // The base case. Nothing to do.
    } else {
      // Note that we are constructing the blockInfos so that the order of the blocks in the blockInfos is from the oldest to the newest.
      blockInfos.prepend(last)
      val beforeLastOption : Option[BlockInfo] = storage.getBlockInfo(last.blockHeader.hashPrevBlock)
      assert(beforeLastOption.isDefined)
      collectBlockInfos(blockInfos, beforeFirst, beforeLastOption.get)
    }
  }

  /**
    * Attach blocks to the best blockchain. Oldest blocks are attached first. (Attach order : Oldest -> Newest )
    *
    * @param beforeFirst Blocks after this block are attached to the best blockchain.
    * @param last The last block in the new best blockchain.
    */
  protected[chain] def attachBlocksAfter(beforeFirst : BlockInfo, last : BlockInfo) : Unit = {
    // Blocks NOT in the best blockchain does not have the BlockInfo.nextBlockHash.
    // We need to track from the last back to beforeFirst, and reverse the list.
    val blockInfos = ListBuffer[BlockInfo]()
    collectBlockInfos(blockInfos, beforeFirst, last)

    var prevBlockHash = beforeFirst.blockHeader.hash
    // The previous block of the first block in the list buffer should be the beforeFirst block.
    assert(prevBlockHash == blockInfos.head.blockHeader.hashPrevBlock)
    // The last block info in the list buffer should match the last block info passed to this method.
    assert(last == blockInfos.last)

    blockInfos foreach { blockInfo : BlockInfo =>
      val blockHash = blockInfo.blockHeader.hash
      // Get the block
      val Some((readBlockInfo, readBlock)) = storage.getBlock(blockHash)
      assert(readBlockInfo == blockInfo)

      attachBlock( readBlockInfo, readBlock )
      // Link the next block hash.
      storage.updateNextBlockHash(prevBlockHash, Some(blockHash))

      prevBlockHash = blockHash
    }

    // The last block should not have any next block.
    assert( last.nextBlockHash.isEmpty )
  }


  /** Reorganize blocks.
    * This method is called when the new best block is not based on the original best block.
    *
    * @param originalBestBlock The original best block before the new best one was found.
    * @param newBestBlock The new best block, which has greater chain work than the original best block.
    */
  protected[chain] def reorganize(originalBestBlock : BlockInfo, newBestBlock : BlockInfo) : Unit = {
    // TODO : BUGBUG : Need to think about RocksDB transactions.

    assert( originalBestBlock.chainWork < newBestBlock.chainWork)

    // Step 1 : Find the common block(pfork) between the current blockchain(pindexBest) and the new longer blockchain.
    val commonBlock : BlockInfo = findCommonBlock(originalBestBlock, newBestBlock)

    // TODO : Call chainEventListener : onNewBlock, onRemoveBlock

    // Step 2 : Detach blocks after the common block to originalBestBlock, which is the tip of the current best blockchain.
    val Some((bestBlockInfo, bestBlock )) = storage.getBlock(originalBestBlock.blockHeader.hash)
    assert(bestBlockInfo == originalBestBlock)
    detachBlocksAfter(commonBlock, bestBlockInfo, bestBlock)


    // Step 3 : Attach blocks after the common block to the newBestBlock.
    attachBlocksAfter(commonBlock, newBestBlock)
    /*



      // Step 2 : Get the list of blocks to disconnect from the common block to the tip of the current blockchain

      // Step 3 : Get the list of blocks to connect from the common block to the longer blockchain.

      // Step 4 : Reorder the list of blocks to connect so that the blocks with lower height come first.

      // Step 5 : Disconnect blocks from the current (shorter) blockchain. (order : newest to oldest)
      LOOP block := For each block to disconnect
          // Step 5.1 : Read each block, and disconnect each block.
          block.ReadFromDisk(pindex)
          block.DisconnectBlock(txdb, pindex)
              1. Mark all outputs spent by all inputs of all transactions in the block as unspent.
              LOOP tx := For each transaction in the block
                  1.1. Mark outputs pointed by the inputs of the transaction unspent.
                  tx.DisconnectInputs
                      - LOOP input := For each input in the transaction
                          - Get the transaction pointed by the input
                          - On disk, Mark the output point by the input as spent
              2. On disk, disconnect from the previous block
                  (previous block.next = null)

          // Step 5.2 : Prepare transactions to add back to the mempool.
      }

      // Step 6 : Connect blocks from the longer blockchain to the current blockchain. (order : oldest to newest)
      LOOP block := For each block to connect
          // kangmo : comment - Step 6.1 : Read block, connect the block to the current blockchain, which does not have the disconnected blocks.
          block.ReadFromDisk(pindex)
          block.ConnectBlock(txdb, pindex)
              - 1. Do preliminary checks for a block
              pblock->CheckBlock()

              - 2. Prepare a queue for database changes marking outputs spent by all inputs of all transactions in the block.
              map<uint256, CTxIndex> mapQueuedChanges;

              - 3. Populate mapQueuedChanges with transaction outputs marking which transactions are spending each of the outputs.
              LOOP tx := For each transaction in the block
                  - 3.1 Mark transaction outputs pointed by inputs of this transaction spent.
                  IF not coinbase transaction
                      CTransaction::ConnectInputs( .. & mapQueuedChanges .. )
                          LOOP input := for each input in the transaction
                              // 1. read CTxIndex from disk if not read yet.
                              // 2. read the transaction that the outpoint points from disk if not read yet.
                              // 3. Increase DoS score if an invalid output index was found in a transaction input.
                              // 4. check coinbase maturity for outpoints spent by a transaction.
                              // 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
                              // 6. check double spends for each OutPoint of each transaction input in a transaction.
                              // 7. check value range of each input and sum of inputs.
                              // 8. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output.

                          // check if the sum of input values is greater than or equal to the sum of outputs.
                          // make sure if the fee is not negative.
                          // check the minimum transaction fee for each transaction.
                  // Add UTXO : set all outputs are unspent for the newly connected transaction.

              - 4. For each items in mapQueuedChanges, write to disk.
              - 5. Check if the generation transaction's output amount is less than or equal to the reward + sum of fees for all transactions in the block.
              - 6. On disk, connect the block from the previous block. (previous block.next = this block)

              - 7. For each transaction, sync with wallet.
              LOOP tx := For each transaction in the block
                  SyncWithWallets
                      - For each registered wallet
                           pwallet->AddToWalletIfInvolvingMe
          // Step 6.2 : Prepare transactions to remove from the mempool
      }

      // Step 7 : Write the hash of the tip block on the best blockchain, commit the db transaction.

      // Step 8 : Set the next block pointer for each connected block. Also, set next block pointer to null for each disconnected block.
      // Note : next pointers of in-memory block index nodes are modified after the on-disk transaction commits the on-disk version of the next pointers.

      // Step 9 : add transactions in the disconnected blocks to the mempool.

      // Step 10 : Remove transactions in the connected blocks from the mempool.
*/
  }

  /** Get the descriptor of the common ancestor of the two given blocks.
    *
    * @param block1 The first given block.
    * @param block2 The second given block.
    */
  protected def findCommonBlock(block1 : BlockInfo, block2 : BlockInfo) : BlockInfo = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Return an iterator that iterates each ChainBlock from a given height.
    *
    * Used by : importAddress RPC to rescan the blockchain.
    *
    * @param height Specifies where we start the iteration. The height 0 means the genesis block.
    * @return The iterator that iterates each ChainBlock.
    */
  def getIterator(height : Long) : Iterator[ChainBlock] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Return the block height of the best block.
    *
    * @return The best block height.
    */
  def getBestBlockHeight() : Long = {
    assert(theBestBlock != null)
    theBestBlock.height
  }

  /** Return the hash of block on the tip of the best blockchain.
    *
    * @return The best block hash.
    */
  def getBestBlockHash() : Option[Hash] = {
    storage.getBestBlockHash()
  }


  /** Get the hash of a block specified by the block height on the best blockchain.
    *
    * Used by : getblockhash RPC.
    *
    * @param blockHeight The height of the block.
    * @return The hash of the block header.
    */
  def getBlockHash(blockHeight : Long) : Hash = {
    val blockHashOption = storage.getBlockHashByHeight(blockHeight)
    // TODO : Bitcoin Compatiblity : Make the error code compatible when the block height was a wrong value.
    if (blockHashOption.isEmpty) {

      throw new ChainException( ErrorCode.InvalidBlockHeight)
    }
    blockHashOption.get
  }

  /**
    * Used by BlockLocator to get the info of the given block.
    * @param blockHash The hash of the block to get the info of it.
    * @return Some(blockInfo) if the block exists; None otherwise.
    */
  def getBlockInfo(blockHash : Hash) : Option[BlockInfo] = {
    storage.getBlockInfo(blockHash)
  }



  /** See if a block exists on the blockchain.
    *
    * Used by : submitblock RPC to check if a block already exists.
    *
    * @param blockHash The hash of the block header to check.
    * @return true if the block exists; false otherwise.
    */
  def hasBlock(blockHash : Hash) : Boolean = {
    storage.hasBlock(blockHash)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)] = {
    storage.getBlock(blockHash)
  }
  /** Return a transaction that matches the given transaction hash.
    *
    * Used by listtransaction RPC to get the
    *
    * @param txHash The transaction hash to search.
    * @return Some(transaction) if the transaction that matches the hash was found. None otherwise.
    */
  def getTransaction(txHash : Hash) : Option[Transaction] = {
    // Note : No need to search transaction pool, as storage.getTransaction searches the transaction pool as well.

    // Step 1 : Search block database.
    val dbTransactionOption = storage.getTransaction( txHash )

    // Step 3 : TODO : Run validation.

    //BUGBUG : Transaction validation fails because the transaction hash on the outpoint does not exist.
    //poolTransactionOption.foreach( new TransactionVerifier(_).verify(DiskBlockStorage.get) )
    //dbTransactionOption.foreach( new TransactionVerifier(_).verify(DiskBlockStorage.get) )

    dbTransactionOption
  }

  /** Return a transaction output specified by a give out point.
    *
    * @param outPoint The outpoint that points to the transaction output.
    * @return The transaction output we found.
    */
  def getTransactionOutput(outPoint : OutPoint) : TransactionOutput = {
    // Coinbase outpoints should never come here
    assert( !outPoint.transactionHash.isAllZero() )

    val transaction = getTransaction(outPoint.transactionHash)
    if (transaction.isEmpty) {
      throw new ChainException(ErrorCode.InvalidOutPoint, "The transaction was not found : " + outPoint.transactionHash)
    }

    val outputs = transaction.get.outputs

    if( outPoint.outputIndex >= outputs.length) {
      throw new ChainException(ErrorCode.InvalidOutPoint, s"Invalid output index. Transaction hash : ${outPoint.transactionHash}, Output count : ${outputs.length}, Output index : ${outPoint.outputIndex}")
    }

    outputs(outPoint.outputIndex)
  }


  /** Get the current difficulty of block hash.
    *
    * @return
    */
  def getDifficulty() : Long = {
    calculateDifficulty(prevBlockDesc = theBestBlock)
  }

  /** Get the amount of reward that a minder gets from the generation input.
    *
    * @return
    */
  def getCoinbaseAmount() : CoinAmount = {
    // TODO : Implement
    CoinAmount(50)
  }
}

