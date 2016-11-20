package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.BlockchainView

/**
  * Analyze a transaction
  */
object TransactionAnalyzer {

  protected <chain> fun sumAmount(outputs : List<TransactionOutput>) : scala.math.BigDecimal {
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
  fun calculateFee(blockchainView : BlockchainView, transaction : Transaction)(implicit db : KeyValueDatabase) : scala.math.BigDecimal {
    // We can't calculate the fee for the generation transaction.
    assert(!transaction.inputs(0).isCoinBaseInput())

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
  fun getSpentOutputs(blockchainView : BlockchainView, transaction : Transaction)(implicit db : KeyValueDatabase) : List<TransactionOutput> {
    transaction.inputs.map { transactionInput : TransactionInput =>
      blockchainView.getTransactionOutput(OutPoint(
        Hash( transactionInput.outputTransactionHash.value ),
        transactionInput.outputIndex.toInt
      ))
    }
  }
}
