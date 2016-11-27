package io.scalechain.blockchain.chain

import java.util.concurrent.locks.Lock

import com.google.common.util.concurrent.Striped
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.TransactionDescriptorIndex
import io.scalechain.blockchain.transaction.ChainBlock
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ChainException
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.TransactionTimeIndex
import io.scalechain.blockchain.storage.TransactionPoolIndex
import io.scalechain.blockchain.script.hash
import io.scalechain.util.HexUtil
import io.scalechain.util.ListExt
import org.slf4j.LoggerFactory


/**
  * The transaction maganet which is able to attach or detach transactions.
  *
  * @param txDescIndex The storage for block.
  * @param txPoolIndex The storage for transaction pool. If not given, set to storage.
  *                      During mining, txPoolStorage is a separate transaction pool for testing dependency of each transaction.
  *                      Otherwise, txPoolStorage is the 'storage' parameter.
  */
class TransactionMagnet(private val txDescIndex : TransactionDescriptorIndex, private val txPoolIndex: TransactionPoolIndex, private val txTimeIndex : TransactionTimeIndex) {
  private val logger = LoggerFactory.getLogger(TransactionMagnet::class.java)

  protected var chainEventListener : ChainEventListener? = null

  /** Set an event listener of the blockchain.
    *
    * @param listener The listener that wants to be notified for blocks, invalidated blocks, and transactions comes into and goes out from the transaction pool.
    */
  fun setEventListener( listener : ChainEventListener ): Unit {
    chainEventListener = listener
  }

  /**
    * Get the list of in-points that are spending the outputs of a transaction
    *
    * @param txHash The hash of the transaction.
    * @return The list of in-points that are spending the outputs of the transaction
    */
  protected fun getOutputsSpentBy(db : KeyValueDatabase, txHash : Hash) : List<InPoint?>? {
    return txDescIndex.getTransactionDescriptor(db, txHash)?.outputsSpentBy ?:
              txPoolIndex.getTransactionFromPool(db, txHash)?.outputsSpentBy
  }

  /**
    * Put the list of in-points that are spending the outputs of a transaction
    *
    * @param txHash The hash of the transaction.
    * @param outputsSpentBy The list of in-points that are spending the outputs of the transaction
    */
  protected fun putOutputsSpentBy(db : KeyValueDatabase, txHash : Hash, outputsSpentBy : List<InPoint?>) {
    val txDescOption = txDescIndex.getTransactionDescriptor(db, txHash)
    val txPoolEntryOption = txPoolIndex.getTransactionFromPool(db, txHash)
    if ( txDescOption != null ) {
      txDescIndex.putTransactionDescriptor(
        db,
        txHash,
        txDescOption.copy(
          outputsSpentBy = outputsSpentBy
        )
      )
      // Note that txPoolEntryOption can be defined,
      // because the same transaction can be attached at the same time while (1) attaching a block by putBlock (2) attaching a transaction by putTransaction
    } else {
      if (txPoolEntryOption == null) throw AssertionError()

      txPoolIndex.putTransactionToPool(
        db,
        txHash,
        txPoolEntryOption.copy(
          outputsSpentBy = outputsSpentBy
        )
      )
    }
  }

  /**
    * Mark an output spent by the given in-point.
    *
    * @param outPoint The out-point that points to the output to mark.
    * @param inPoint The in-point that points to a transaction input that spends to output.
    * @param checkOnly If true, do not update the spending in-point, just check if the output is a valid UTXO.
    */
  protected fun markOutputSpent(db : KeyValueDatabase, outPoint : OutPoint, inPoint : InPoint, checkOnly : Boolean): Unit {
    val outputsSpentBy : List<InPoint?>? = getOutputsSpentBy(db, outPoint.transactionHash)
    if (outputsSpentBy == null) {
      val message = "An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) points to a transaction that does not exist yet."
      if (!checkOnly)
        logger.warn(message)
      throw ChainException(ErrorCode.ParentTransactionNotFound, message)
    }

    // TODO : BUGBUG : indexing into a list is slow. Optimize the code.
    if ( outPoint.outputIndex < 0 || outputsSpentBy.size <= outPoint.outputIndex ) {
      // TODO : Add DoS score. The outpoint in a transaction input was invalid.
      val message = "An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) has invalid transaction output index."
      if (!checkOnly)
        logger.warn(message)
      throw ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    val spendingInPointOption = outputsSpentBy[outPoint.outputIndex]
    if( spendingInPointOption != null ) { // The transaction output was already spent.
      if ( spendingInPointOption == inPoint ) {
        // Already marked as spent by the given in-point.
        // This can happen when a transaction is already attached while it was put into the transaction pool,
        // But tried to attach again while accepting a block that has the (already attached) transaction.
      } else {
        val message = "An output pointed by an out-point(${outPoint}) has already been spent by ${spendingInPointOption}. The in-point(${inPoint}) tried to spend it again."
        if (!checkOnly)
          logger.warn(message);
        throw ChainException(ErrorCode.TransactionOutputAlreadySpent, message)
      }
    } else {
      if (checkOnly) {
        // Do not update, just check if the output can be marked as spent.
      } else {
        putOutputsSpentBy(
          db,
          outPoint.transactionHash,
          outputsSpentBy.mapIndexed { i, originalInPoint ->
            if (i == outPoint.outputIndex) inPoint else originalInPoint
          }
        )
      }
    }
  }

  /**
    * Mark an output unspent. The output should have been marked as spent by the given in-point.
    *
    * @param outPoint The out-point that points to the output to mark.
    * @param inPoint The in-point that points to a transaction input that should have spent the output.
    */
  protected fun markOutputUnspent(db : KeyValueDatabase, outPoint : OutPoint, inPoint : InPoint) : Unit {
    val outputsSpentBy : List<InPoint?>? = getOutputsSpentBy(db, outPoint.transactionHash)
    if (outputsSpentBy == null) {
      val message = "An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) points to a transaction that does not exist."
      logger.warn(message)
      throw ChainException(ErrorCode.ParentTransactionNotFound, message)
    }

    // TODO : BUGBUG : indexing into a list is slow. Optimize the code.
    if ( outPoint.outputIndex < 0 || outputsSpentBy.size <= outPoint.outputIndex ) {
      // TODO : Add DoS score. The outpoint in a transaction input was invalid.
      val message = "An output pointed by an out-point(${outPoint}) has invalid transaction output index. The output should have been spent by ${inPoint}"
      logger.warn(message)
      throw ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    val spendingInPointOption = outputsSpentBy[outPoint.outputIndex]
    // The output pointed by the out-point should have been spent by the transaction input poined by the given in-point.

    if( spendingInPointOption!! != inPoint ) { // The transaction output was NOT spent by the transaction input poined by the given in-point.
    val message = "An output pointed by an out-point(${outPoint}) was not spent by the expected transaction input pointed by the in-point(${inPoint}), but spent by ${spendingInPointOption}."
      logger.warn(message)
      throw ChainException(ErrorCode.TransactionOutputSpentByUnexpectedInput, message)
    }

    putOutputsSpentBy(
      db,
      outPoint.transactionHash,
      outputsSpentBy.mapIndexed { i, originalInPoint ->
        if (i == outPoint.outputIndex) null else originalInPoint
      }
    )
  }

  /**
    * Detach the transaction input from the best blockchain.
    * The output spent by the transaction input is marked as unspent.
    *
    * @param inPoint The in-point that points to the input to attach.
    * @param transactionInput The transaction input to attach.
    */
  protected fun detachTransactionInput(db : KeyValueDatabase, inPoint : InPoint, transactionInput : TransactionInput) : Unit {
    // Make sure that the transaction input is not a coinbase input. detachBlock already checked if the input was NOT coinbase.
    assert(!transactionInput.isCoinBaseInput())

    markOutputUnspent(db, transactionInput.getOutPoint(), inPoint)
  }

  /**
    * Detach each of transction inputs. Mark outputs spent by the transaction inputs unspent.
    *
    * @param transactionHash The hash of the tranasction that has the inputs.
    * @param transaction The transaction that has the inputs.
    */
  protected fun detachTransactionInputs(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction) : Unit {
    var inputIndex = -1
    transaction.inputs.forEach{ transactionInput ->
      inputIndex += 1

      // Make sure that the transaction input is not a coinbase input. detachBlock already checked if the input was NOT coinbase.
      assert(!transactionInput.isCoinBaseInput())

      detachTransactionInput(db, InPoint(transactionHash, inputIndex), transactionInput)
    }
  }

  /**
    * Detach the transaction from the best blockchain.
    *
    * For outputs, all outputs spent by the transaction is marked as unspent.
    *
    * @param transaction The transaction to detach.
    */
  fun detachTransaction(db : KeyValueDatabase, transaction : Transaction) : Unit {
    val transactionHash = transaction.hash()

    // Step 1 : Detach each transaction input
    if (transaction.inputs[0].isCoinBaseInput()) {
      // Nothing to do for the coinbase inputs.
    } else {
      detachTransactionInputs(db, transactionHash, transaction)
    }

    // Remove the transaction descriptor otherwise other transactions can spend the UTXO from the detached transaction.
    // The transaction might not be stored in a block on the best blockchain yet. Remove the transaction from the pool too.
    txDescIndex.delTransactionDescriptor(db, transactionHash)

    val txOption : TransactionPoolEntry? = txPoolIndex.getTransactionFromPool(db, transactionHash)
    if (txOption != null) {
      // BUGBUG : Need to remove these two records atomically
      txTimeIndex.delTransactionTime( db, txOption.createdAtNanos, transactionHash)
      txPoolIndex.delTransactionFromPool(db, transactionHash)
    }

    chainEventListener?.onRemoveTransaction(db, transactionHash, transaction)
  }

  /**
    * The UTXO pointed by the transaction input is marked as spent by the in-point.
    *
    * @param inPoint The in-point that points to the input to attach.
    * @param transactionInput The transaction input to attach.
    * @param checkOnly If true, do not attach the transaction input, but just check if the transaction input can be attached.
    *
    */
  protected fun attachTransactionInput(db : KeyValueDatabase, inPoint : InPoint, transactionInput : TransactionInput, checkOnly : Boolean) : Unit {
    // Make sure that the transaction input is not a coinbase input. attachBlock already checked if the input was NOT coinbase.
    assert(!transactionInput.isCoinBaseInput())

    // TODO : Step 1. read CTxIndex from disk if not read yet.
    // TODO : Step 2. read the transaction that the outpoint points from disk if not read yet.
    // TODO : Step 3. Increase DoS score if an invalid output index was found in a transaction input.
    // TODO : Step 4. check coinbase maturity for outpoints spent by a transaction.
    // TODO : Step 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
    // TODO : Step 6. check value range of each input and sum of inputs.
    // TODO : Step 7. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output. check double spends.
    markOutputSpent(db, transactionInput.getOutPoint(), inPoint, checkOnly)
  }

  /** Attach the transaction inputs to the outputs spent by them.
    * Mark outputs spent by the transaction inputs.
    *
    * @param transactionHash The hash of the tranasction that has the inputs.
    * @param transaction The transaction that has the inputs.
    * @param checkOnly If true, do not attach the transaction inputs, but just check if the transaction inputs can be attached.
    *
    */
  protected fun attachTransactionInputs(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction, checkOnly : Boolean) : Unit {
    var inputIndex = -1
    transaction.inputs.forEach{ transactionInput ->
      // Make sure that the transaction input is not a coinbase input. attachBlock already checked if the input was NOT coinbase.
      assert(!transactionInput.isCoinBaseInput())
      inputIndex += 1

      attachTransactionInput(db, InPoint(transactionHash, inputIndex), transactionInput, checkOnly)
    }
  }

  /**
    * Attach the transaction into the best blockchain.
    *
    * For UTXOs, all outputs spent by the transaction is marked as spent by this transaction.
    *
    * @param transactionHash The hash of the transaction to attach.
    * @param transaction The transaction to attach.
    * @param checkOnly If true, do not attach the transaction inputs, but just check if the transaction inputs can be attached.
    * @param txLocatorOption Some(locator) if the transaction is stored in a block on the best blockchain; None if the transaction should be stored in a mempool.
    */
  fun attachTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : Transaction, checkOnly : Boolean, txLocatorOption : FileRecordLocator? = null, chainBlock : ChainBlock? = null, transactionIndex : Int? = null) : Unit {
    //logger.trace(s"Attach Transaction : ${transactionHash}, stack : ${StackUtil.getCurrentStack}")
    // Step 1 : Attach each transaction input
    if (transaction.inputs[0].isCoinBaseInput()) {
      // Nothing to do for the coinbase inputs.
    } else {
      attachTransactionInputs(db, transactionHash, transaction, checkOnly)
    }

    if (checkOnly) {
      // Do nothing. We just want to check if we can attach the transaction.
    } else {
      // Need to set the transaction locator of the transaction descriptor according to the location of the attached block.
      if (txLocatorOption != null) {
        //logger.trace(s"<Attach Transaction> Put transaction descriptor : ${transactionHash}")
        // If the txLocator is defined, the block height should also be defined.

        txDescIndex.putTransactionDescriptor(
          db,
          transactionHash,
          TransactionDescriptor(
            transactionLocator = txLocatorOption,
            blockHeight = chainBlock!!.height,
            outputsSpentBy = ListExt.fill<InPoint?>( transaction.outputs.size, null)
          )
        )
      } else {
        // Use fine grained lock for the concurrency control of adding a transaction.
        // To fix Issue : #105 Remove duplicate transactions in blocks
        // https://github.com/ScaleChain/scalechain/issues/105
        val txLockName = HexUtil.hex(transactionHash.value)

        val txLock = TransactionMagnet.txLock.get(txLockName)

        txLock.lock()
        try {
          if ( txPoolIndex.getTransactionFromPool(db, transactionHash) == null ) {
            //logger.trace(s"<Attach Transaction> Put into the pool : ${transactionHash}")
            val txCreatedAt = System.nanoTime()
            // Need to put transaction first, and then put transaction time.
            // Why? We will search by transaction time, and get the transaction object from tx hash we get from the transaction time index.
            // If we put transaction time first, we may not have transaction even though a transaction time exists.
            txPoolIndex.putTransactionToPool(
              db,
              transactionHash,
              TransactionPoolEntry(
                transaction,
                ListExt.fill<InPoint?>(transaction.outputs.size, null),
                txCreatedAt
              )
            )
            txTimeIndex.putTransactionTime(db, txCreatedAt, transactionHash)
          }
        } finally {
          txLock.unlock()
        }
      }

      chainEventListener?.onNewTransaction(db, transactionHash, transaction, chainBlock, transactionIndex)
    }

    // TODO : Step 2 : check if the sum of input values is greater than or equal to the sum of outputs.
    // TODO : Step 3 : make sure if the fee is not negative.
    // TODO : Step 4 : check the minimum transaction fee for each transaction.
  }

  companion object {
    val TxLockCount = 1024
    val txLock : Striped<Lock> = Striped.lock(TxLockCount);
  }
}
