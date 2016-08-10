package io.scalechain.blockchain.chain

import com.typesafe.scalalogging.Logger
import de.jkeylockmanager.manager.{LockCallback, KeyLockManager, KeyLockManagers}
import io.scalechain.blockchain.storage.index.{KeyValueDatabase, TransactionDescriptorIndex}
import io.scalechain.blockchain.transaction.ChainBlock
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.{TransactionTimeIndex, TransactionPoolIndex, TransactionLocator, BlockStorage}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.util.{HexUtil, StackUtil}
import org.slf4j.LoggerFactory


object TransactionMagnet {

  val txLock : KeyLockManager = KeyLockManagers.newLock();
}
/**
  * The transaction maganet which is able to attach or detach transactions.
  *
  * @param txDescIndex The storage for block.
  * @param txPoolIndex The storage for transaction pool. If not given, set to storage.
  *                      During mining, txPoolStorage is a separate transaction pool for testing dependency of each transaction.
  *                      Otherwise, txPoolStorage is the 'storage' parameter.
  */
class TransactionMagnet(txDescIndex : TransactionDescriptorIndex, txPoolIndex: TransactionPoolIndex, txTimeIndex : TransactionTimeIndex) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[TransactionMagnet]) )

  protected [chain] var chainEventListener : Option[ChainEventListener] = None

  /** Set an event listener of the blockchain.
    *
    * @param listener The listener that wants to be notified for new blocks, invalidated blocks, and transactions comes into and goes out from the transaction pool.
    */
  def setEventListener( listener : ChainEventListener ): Unit = {
    chainEventListener = Some(listener)
  }

  /**
    * Get the list of in-points that are spending the outputs of a transaction
    *
    * @param txHash The hash of the transaction.
    * @return The list of in-points that are spending the outputs of the transaction
    */
  protected[chain] def getOutputsSpentBy(txHash : Hash)(implicit db : KeyValueDatabase) : List[Option[InPoint]] = {
    txDescIndex.getTransactionDescriptor(txHash).map(_.outputsSpentBy).getOrElse {
      txPoolIndex.getTransactionFromPool(txHash).map(_.outputsSpentBy).getOrElse {
        null
      }
    }
  }

  /**
    * Put the list of in-points that are spending the outputs of a transaction
    *
    * @param txHash The hash of the transaction.
    * @param outputsSpentBy The list of in-points that are spending the outputs of the transaction
    */
  protected[chain] def putOutputsSpentBy(txHash : Hash, outputsSpentBy : List[Option[InPoint]])(implicit db : KeyValueDatabase) = {
    val txDescOption = txDescIndex.getTransactionDescriptor(txHash)
    val txPoolEntryOption = txPoolIndex.getTransactionFromPool(txHash)
    if ( txDescOption.isDefined) {
      txDescIndex.putTransactionDescriptor(
        txHash,
        txDescOption.get.copy(
          outputsSpentBy = outputsSpentBy
        )
      )
      // Note that txPoolEntryOption can be defined,
      // because the same transaction can be attached at the same time while (1) attaching a block by putBlock (2) attaching a transaction by putTransaction
    } else {
      assert( txPoolEntryOption.isDefined )
      txPoolIndex.putTransactionToPool(
        txHash,
        txPoolEntryOption.get.copy(
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
  protected[chain] def markOutputSpent(outPoint : OutPoint, inPoint : InPoint, checkOnly : Boolean)(implicit db : KeyValueDatabase): Unit = {
    val outputsSpentBy : List[Option[InPoint]] = getOutputsSpentBy(outPoint.transactionHash)
    if (outputsSpentBy == null) {
      val message = s"An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) points to a transaction that does not exist yet."
      if (!checkOnly)
        logger.warn(message)
      throw new ChainException(ErrorCode.ParentTransactionNotFound, message)
    }

    // TODO : BUGBUG : indexing into a list is slow. Optimize the code.
    if ( outPoint.outputIndex < 0 || outputsSpentBy.length <= outPoint.outputIndex ) {
      // TODO : Add DoS score. The outpoint in a transaction input was invalid.
      val message = s"An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) has invalid transaction output index."
      if (!checkOnly)
        logger.warn(message)
      throw new ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    val spendingInPointOption = outputsSpentBy(outPoint.outputIndex)
    if( spendingInPointOption.isDefined ) { // The transaction output was already spent.
      if ( spendingInPointOption.get == inPoint ) {
        // Already marked as spent by the given in-point.
        // This can happen when a transaction is already attached while it was put into the transaction pool,
        // But tried to attach again while accepting a block that has the (already attached) transaction.
      } else {
        val message = s"An output pointed by an out-point(${outPoint}) has already been spent by ${spendingInPointOption.get}. The in-point(${inPoint}) tried to spend it again."
        if (!checkOnly)
          logger.warn(message);
        throw new ChainException(ErrorCode.TransactionOutputAlreadySpent, message)
      }
    } else {
      if (checkOnly) {
        // Do not update, just check if the output can be marked as spent.
      } else {
        putOutputsSpentBy(
          outPoint.transactionHash,
          outputsSpentBy.updated(outPoint.outputIndex, Some(inPoint))
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
  protected[chain] def markOutputUnspent(outPoint : OutPoint, inPoint : InPoint)(implicit db : KeyValueDatabase): Unit = {
    val outputsSpentBy : List[Option[InPoint]] = getOutputsSpentBy(outPoint.transactionHash)
    if (outputsSpentBy == null) {
      val message = s"An output pointed by an out-point(${outPoint}) spent by the in-point(${inPoint}) points to a transaction that does not exist."
      logger.warn(message)
      throw new ChainException(ErrorCode.ParentTransactionNotFound, message)
    }

    // TODO : BUGBUG : indexing into a list is slow. Optimize the code.
    if ( outPoint.outputIndex < 0 || outputsSpentBy.length <= outPoint.outputIndex ) {
      // TODO : Add DoS score. The outpoint in a transaction input was invalid.
      val message = s"An output pointed by an out-point(${outPoint}) has invalid transaction output index. The output should have been spent by ${inPoint}"
      logger.warn(message)
      throw new ChainException(ErrorCode.InvalidTransactionOutPoint, message)
    }

    val spendingInPointOption = outputsSpentBy(outPoint.outputIndex)
    // The output pointed by the out-point should have been spent by the transaction input poined by the given in-point.
    assert( spendingInPointOption.isDefined )

    if( spendingInPointOption.get != inPoint ) { // The transaction output was NOT spent by the transaction input poined by the given in-point.
    val message = s"An output pointed by an out-point(${outPoint}) was not spent by the expected transaction input pointed by the in-point(${inPoint}), but spent by ${spendingInPointOption.get}."
      logger.warn(message)
      throw new ChainException(ErrorCode.TransactionOutputSpentByUnexpectedInput, message)
    }

    putOutputsSpentBy(
      outPoint.transactionHash,
      outputsSpentBy.updated(outPoint.outputIndex, None)
    )
  }

  /**
    * Detach the transaction input from the best blockchain.
    * The output spent by the transaction input is marked as unspent.
    *
    * @param inPoint The in-point that points to the input to attach.
    * @param transactionInput The transaction input to attach.
    */
  protected[chain] def detachTransactionInput(inPoint : InPoint, transactionInput : TransactionInput)(implicit db : KeyValueDatabase) : Unit = {
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
  protected[chain] def detachTransactionInputs(transactionHash : Hash, transaction : Transaction)(implicit db : KeyValueDatabase) : Unit = {
    var inputIndex = -1
    transaction.inputs foreach { transactionInput : TransactionInput =>
      inputIndex += 1

      // Make sure that the transaction input is not a coinbase input. detachBlock already checked if the input was NOT coinbase.
      assert(!transactionInput.isCoinBaseInput())

      detachTransactionInput(InPoint(transactionHash, inputIndex), transactionInput)
    }
  }

  /**
    * Detach the transaction from the best blockchain.
    *
    * For outputs, all outputs spent by the transaction is marked as unspent.
    *
    * @param transaction The transaction to detach.
    */
  def detachTransaction(transaction : Transaction)(implicit db : KeyValueDatabase) : Unit = {
    val transactionHash = transaction.hash

    // Step 1 : Detach each transaction input
    if (transaction.inputs(0).isCoinBaseInput()) {
      // Nothing to do for the coinbase inputs.
    } else {
      detachTransactionInputs(transactionHash, transaction)
    }

    // Remove the transaction descriptor otherwise other transactions can spend the UTXO from the detached transaction.
    // The transaction might not be stored in a block on the best blockchain yet. Remove the transaction from the pool too.
    txDescIndex.delTransactionDescriptor(transactionHash)

    val txOption : Option[TransactionPoolEntry] = txPoolIndex.getTransactionFromPool(transactionHash)
    if (txOption.isDefined) {
      // BUGBUG : Need to remove these two records atomically
      txTimeIndex.delTransactionTime( txOption.get.createdAtNanos, transactionHash)
      txPoolIndex.delTransactionFromPool(transactionHash)
    }

    chainEventListener.map(_.onRemoveTransaction(transactionHash, transaction))
  }

  /**
    * The UTXO pointed by the transaction input is marked as spent by the in-point.
    *
    * @param inPoint The in-point that points to the input to attach.
    * @param transactionInput The transaction input to attach.
    * @param checkOnly If true, do not attach the transaction input, but just check if the transaction input can be attached.
    *
    */
  protected[chain] def attachTransactionInput(inPoint : InPoint, transactionInput : TransactionInput, checkOnly : Boolean)(implicit db : KeyValueDatabase) : Unit = {
    // Make sure that the transaction input is not a coinbase input. attachBlock already checked if the input was NOT coinbase.
    assert(!transactionInput.isCoinBaseInput())

    // TODO : Step 1. read CTxIndex from disk if not read yet.
    // TODO : Step 2. read the transaction that the outpoint points from disk if not read yet.
    // TODO : Step 3. Increase DoS score if an invalid output index was found in a transaction input.
    // TODO : Step 4. check coinbase maturity for outpoints spent by a transaction.
    // TODO : Step 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
    // TODO : Step 6. check value range of each input and sum of inputs.
    // TODO : Step 7. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output. check double spends.
    markOutputSpent(transactionInput.getOutPoint(), inPoint, checkOnly)
  }

  /** Attach the transaction inputs to the outputs spent by them.
    * Mark outputs spent by the transaction inputs.
    *
    * @param transactionHash The hash of the tranasction that has the inputs.
    * @param transaction The transaction that has the inputs.
    * @param checkOnly If true, do not attach the transaction inputs, but just check if the transaction inputs can be attached.
    *
    */
  protected[chain] def attachTransactionInputs(transactionHash : Hash, transaction : Transaction, checkOnly : Boolean)(implicit db : KeyValueDatabase) : Unit = {
    var inputIndex = -1
    transaction.inputs foreach { transactionInput : TransactionInput =>
      // Make sure that the transaction input is not a coinbase input. attachBlock already checked if the input was NOT coinbase.
      assert(!transactionInput.isCoinBaseInput())
      inputIndex += 1

      attachTransactionInput(InPoint(transactionHash, inputIndex), transactionInput, checkOnly)
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
  protected[chain] def attachTransaction(transactionHash : Hash, transaction : Transaction, checkOnly : Boolean, txLocatorOption : Option[FileRecordLocator] = None, chainBlock : Option[ChainBlock] = None, transactionIndex : Option[Int] = None)(implicit db : KeyValueDatabase) : Unit = {
    //logger.trace(s"Attach Transaction : ${transactionHash}, stack : ${StackUtil.getCurrentStack}")
    // Step 1 : Attach each transaction input
    if (transaction.inputs(0).isCoinBaseInput()) {
      // Nothing to do for the coinbase inputs.
    } else {
      attachTransactionInputs(transactionHash, transaction, checkOnly)
    }

    if (checkOnly) {
      // Do nothing. We just want to check if we can attach the transaction.
    } else {
      // Need to set the transaction locator of the transaction descriptor according to the location of the attached block.
      if (txLocatorOption.isDefined) {
        //logger.trace(s"[Attach Transaction] Put transaction descriptor : ${transactionHash}")
        // If the txLocator is defined, the block height should also be defined.
        assert( chainBlock.isDefined )
        txDescIndex.putTransactionDescriptor(transactionHash,
          TransactionDescriptor(
            transactionLocator = txLocatorOption.get,
            blockHeight = chainBlock.get.height,
            List.fill(transaction.outputs.length)(None)
          )
        )
      } else {
        // Use fine grained lock for the concurrency control of adding a transaction.
        // To fix Issue : #105 Remove duplicate transactions in blocks
        // https://github.com/ScaleChain/scalechain/issues/105
        val txLockName = HexUtil.hex(transactionHash.value)
        TransactionMagnet.txLock.executeLocked(txLockName, new LockCallback() {
          def doInLock(): Unit = {
            if ( txPoolIndex.getTransactionFromPool(transactionHash).isEmpty ) {
              //logger.trace(s"[Attach Transaction] Put into the pool : ${transactionHash}")
              val txCreatedAt = System.nanoTime()
              // Need to put transaction first, and then put transaction time.
              // Why? We will search by transaction time, and get the transaction object from tx hash we get from the transaction time index.
              // If we put transaction time first, we may not have transaction even though a transaction time exists.
              txPoolIndex.putTransactionToPool(
                transactionHash,
                TransactionPoolEntry(
                  transaction,
                  List.fill(transaction.outputs.length)(None),
                  txCreatedAt
                )
              )
              txTimeIndex.putTransactionTime(txCreatedAt, transactionHash)
            }
          }
        })
      }

      chainEventListener.map(_.onNewTransaction(transactionHash, transaction, chainBlock, transactionIndex))
    }

    // TODO : Step 2 : check if the sum of input values is greater than or equal to the sum of outputs.
    // TODO : Step 3 : make sure if the fee is not negative.
    // TODO : Step 4 : check the minimum transaction fee for each transaction.
  }
}
