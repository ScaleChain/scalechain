package io.scalechain.blockchain.chain

import java.util.PriorityQueue
import java.util.Comparator

import io.scalechain.blockchain.proto.OutPoint
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.TransactionInput
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAmount
import io.scalechain.blockchain.transaction.CoinsView
import java.math.BigDecimal
import java.math.BigInteger

data class TransactionWithFee(val transaction : Transaction, val fee : CoinAmount)

/**
  * Calculate the transaction fee.
  */
object TransactionFeeCalculator {
  /**
    * Calculate fee. Sum(input values) = Sum(output values)
    *
    * @param coinsView The coins view to get the UTXO.
    * @param tx The transaction to calculate the fee.
    * @return The amount of the fee.
    */
  fun fee(db : KeyValueDatabase, coinsView : CoinsView, tx : Transaction) : CoinAmount {
    val totalInputAmount = tx.inputs.fold(0L, { acc : Long, input : TransactionInput ->
      acc + coinsView.getTransactionOutput( db, OutPoint( input.outputTransactionHash, input.outputIndex.toInt()) ).value
    })

    val totalOutputAmount : Long = tx.outputs.fold(0L, { acc : Long, output : TransactionOutput ->
      acc + output.value
    })
    val feeAmount = totalInputAmount - totalOutputAmount
    return CoinAmount(BigDecimal.valueOf(feeAmount))
  }
}

class DescendingTransactionFeeComparator : Comparator<TransactionWithFee> {
  override fun compare(x : TransactionWithFee, y : TransactionWithFee): Int {
    return - (x.fee.value - y.fee.value).toInt()
  }
}
/**
  * Created by kangmo on 6/30/16.
  */
class TransactionPriorityQueue(private val coinsView : CoinsView) {
  val queue = PriorityQueue( DescendingTransactionFeeComparator() )
  fun enqueue(db : KeyValueDatabase, tx : Transaction) {
    queue.add(TransactionWithFee(tx, TransactionFeeCalculator.fee(db, coinsView, tx) ))
  }
  fun dequeue() : Transaction? {
    val txWithFee = queue.poll()
    return txWithFee?.transaction
  }
}
