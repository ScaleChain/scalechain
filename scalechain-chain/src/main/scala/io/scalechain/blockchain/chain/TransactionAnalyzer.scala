package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto._

/**
  * Analyze a transaction
  */
object TransactionAnalyzer {

  protected [chain] def sumAmount(outputs : List[TransactionOutput]) : scala.math.BigDecimal = {
    outputs.foldLeft( scala.math.BigDecimal(0) ) { (sum, output) =>
      sum + output.value
    }
  }

  /** Calculate fee for a transaction.
    *
    * @param blockchainView The read-only view of the best blockchain.
    * @param transaction The transaction to calculate fee for it.
    * @return
    */
  def calculateFee(blockchainView : BlockchainView, transaction : Transaction) : scala.math.BigDecimal = {
    val sumOfInputAmounts = sumAmount( getSpentOutputs(blockchainView, transaction) )

    val sumOfOutputAmounts = sumAmount( transaction.outputs )

    val fee = sumOfInputAmounts - sumOfOutputAmounts

    fee
  }


  /** Get spent outputs in the transaction.
    *
    * @param blockchainView The read-only view of the best blockchain.
    * @param transaction
    * @return
    */
  def getSpentOutputs(blockchainView : BlockchainView, transaction : Transaction) : List[TransactionOutput] = {
    transaction.inputs.map { transactionInput : TransactionInput =>
      blockchainView.getTransactionOutput(OutPoint(
        Hash( transactionInput.outputTransactionHash.value ),
        transactionInput.outputIndex.toInt
      ))
    }
  }
}
