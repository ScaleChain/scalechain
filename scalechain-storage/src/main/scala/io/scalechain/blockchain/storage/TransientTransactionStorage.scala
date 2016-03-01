package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Hash, TransactionHash, Transaction}
import io.scalechain.blockchain.script.HashCalculator

import scala.collection.mutable

/** Store/Retrieve transactions on a memory pool.
  *
  */
class TransientTransactionStorage extends TransactionStorage {
  val transactionsByHash = mutable.HashMap[Hash, Transaction]()

  def storeTransaction(transaction : Transaction): Unit = {
    val hash = Hash( HashCalculator.transactionHash(transaction) )
    transactionsByHash.put(hash, transaction)
  }

  def getTransaction(hash : Hash ) : Option[Transaction] = {
    transactionsByHash.get(hash)
  }

  def hasTransaction(hash : Hash): Boolean = {
    transactionsByHash.get(hash).isDefined
  }
}
