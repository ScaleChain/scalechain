package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.OutPoint
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap

class TransactionGeneratorBlockchainView : BlockchainView {
  val txMap = ConcurrentHashMap<Hash, Transaction>()
  val availableOutputs = TransactionOutputSet()

  override fun getIterator(db : KeyValueDatabase, height : Long) : Iterator<ChainBlock> {
    throw UnsupportedOperationException()
  }

  override fun getBestBlockHeight() : Long {
    throw UnsupportedOperationException()
  }

  override fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction? {
    return txMap.get(transactionHash)
  }

  override fun getTransactionOutput(db : KeyValueDatabase, outPoint : OutPoint) : TransactionOutput {
    return availableOutputs.getTransactionOutput(db, outPoint)
  }

  fun addTransaction(transaction : Transaction) {
    val txHash = transaction.hash()
    txMap.put(txHash, transaction)

    for ( i in 0 until transaction.outputs.size) {
      val outPoint = OutPoint(txHash, i)
      availableOutputs.addTransactionOutput(outPoint, transaction.outputs[i])
    }
  }
}
