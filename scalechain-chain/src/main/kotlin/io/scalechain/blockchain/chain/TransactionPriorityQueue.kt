package io.scalechain.blockchain.chain

import java.util.{PriorityQueue, Comparator}

import io.scalechain.blockchain.proto.OutPoint
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.TransactionInput
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.CoinAmount
import io.scalechain.blockchain.transaction.CoinsView

case class TransactionWithFee(transaction : Transaction, fee : CoinAmount)

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
  def fee(coinsView : CoinsView, tx : Transaction)(implicit db : KeyValueDatabase) : CoinAmount = {
    val totalInputAmount = tx.inputs.foldLeft(0L) { (acc : Long, input : TransactionInput) =>
      acc + coinsView.getTransactionOutput( OutPoint( input.outputTransactionHash, input.outputIndex.toInt) ).value
    }
    val totalOutputAmount : Long = tx.outputs.foldLeft(0L) { (acc : Long, output : TransactionOutput) =>
      acc + output.value
    }
    val feeAmount = totalInputAmount - totalOutputAmount
    CoinAmount(feeAmount)
  }
}

class DescendingTransactionFeeComparator extends Comparator[TransactionWithFee] {
  override def compare(x : TransactionWithFee, y : TransactionWithFee): Int = {
    - (x.fee.value - y.fee.value).toInt
  }
}
/**
  * Created by kangmo on 6/30/16.
  */
class TransactionPriorityQueue(coinsView : CoinsView) {
  val queue = new PriorityQueue( new DescendingTransactionFeeComparator() )
  def enqueue(tx : Transaction)(implicit db : KeyValueDatabase) = {
    queue.add(TransactionWithFee(tx, TransactionFeeCalculator.fee(coinsView, tx) ))
  }
  def dequeue() : Option[Transaction] = {
    val txWithFee = queue.poll()
    if (txWithFee == null) None else Some(txWithFee.transaction)
  }
}
