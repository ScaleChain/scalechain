package io.scalechain.blockchain.chain

import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.BlockStorage
import io.scalechain.blockchain.script.HashSupported._
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 6/9/16.
  */
class TransactionMagnet(storage : BlockStorage) {
  private val logger = LoggerFactory.getLogger(classOf[TransactionMagnet])

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

    val spendingInPointOption  = txDesc.outputsSpentBy(outPoint.outputIndex)
    if( spendingInPointOption.isDefined ) { // The transaction output was already spent.
    val message = s"An output pointed by an out-point(${outPoint}) has already been spent by ${spendingInPointOption.get}. The in-point(${inPoint}) tried to spend it again."
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

    val spendingInPointOption = txDesc.outputsSpentBy(outPoint.outputIndex)
    // The output pointed by the out-point should have been spent by the transaction input poined by the given in-point.
    assert( spendingInPointOption.isDefined )

    if( spendingInPointOption.get != inPoint ) { // The transaction output was NOT spent by the transaction input poined by the given in-point.
    val message = s"An output pointed by an out-point(${outPoint}) was not spent by the expected transaction input pointed by the in-point(${inPoint}), but spent by ${spendingInPointOption.get}."
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
    * Detach the transaction from the best blockchain.
    *
    * For outputs, all outputs spent by the transaction is marked as unspent.
    *
    * @param transaction The transaction to detach.
    */
  def detachTransaction(transaction : Transaction) : Unit = {
    val transactionHash = transaction.hash
    // Step 1 : Detach each transaction input
    detachTransactionInputs(transactionHash, transaction)

    // TODO : BUGBUG : P0 : Need to reset the transaction locator of the transaction descriptor.
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
  }
}
