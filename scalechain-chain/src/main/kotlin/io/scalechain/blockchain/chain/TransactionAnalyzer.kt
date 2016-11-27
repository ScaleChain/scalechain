package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.BlockchainView

/**
  * Analyze a transaction
  */
object TransactionAnalyzer {

  private fun sumAmount(outputs : List<TransactionOutput>) : java.math.BigDecimal {
    return outputs.fold( java.math.BigDecimal.valueOf(0), { sum, output ->
      sum + java.math.BigDecimal.valueOf(output.value)
    })
  }

  /** Calculate fee for a transaction.
    *
    * @param blockchainView The read-only view of the best blockchain.
    * @param transaction The transaction to calculate fee for it.
    * @return
    */
  fun calculateFee(db : KeyValueDatabase, blockchainView : BlockchainView, transaction : Transaction) : java.math.BigDecimal {
    // We can't calculate the fee for the generation transaction.
    assert(!transaction.inputs[0].isCoinBaseInput())

    val sumOfInputAmounts = sumAmount( getSpentOutputs(db, blockchainView, transaction) )

    val sumOfOutputAmounts = sumAmount( transaction.outputs )

    val fee = sumOfInputAmounts - sumOfOutputAmounts

    return fee
  }


  /** Get spent outputs in the transaction.
    *
    * @param blockchainView The read-only view of the best blockchain.
    * @param transaction
    * @return
    */
  fun getSpentOutputs(db : KeyValueDatabase, blockchainView : BlockchainView, transaction : Transaction) : List<TransactionOutput> {
    return transaction.inputs.map { transactionInput : TransactionInput ->
      blockchainView.getTransactionOutput(
          db,
          OutPoint(
            Hash( transactionInput.outputTransactionHash.value ),
            transactionInput.outputIndex.toInt()
          )
      )
    }
  }
}
