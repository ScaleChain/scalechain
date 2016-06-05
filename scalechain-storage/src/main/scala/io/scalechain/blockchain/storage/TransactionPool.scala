package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.proto.codec.{TransactionCodec, OneByteCodec, HashCodec}
import io.scalechain.blockchain.proto.{Transaction, OneByte, Hash}
import io.scalechain.blockchain.storage.index.DatabaseTablePrefixes._
import io.scalechain.blockchain.storage.index.{DatabaseTablePrefixes, SharedKeyValueDatabase}
import io.scalechain.util.HexUtil._
import io.scalechain.util.Using._

object TransactionPool {
  // A dummy prefix key to list all transactions in the disk-pool.
  val DUMMY_PREFIX_KEY = "0"

}

/**
  * Provides index operations for disk-pool, which keeps transactions on-disk instead of mempool.
  * c.f. Orphan transactions are not stored in the disk-pool.
  */
trait TransactionPool extends SharedKeyValueDatabase {
  import TransactionPool._
  import DatabaseTablePrefixes._
  private implicit val hashCodec = HashCodec
  private implicit val transactionCodec = TransactionCodec

  /** Put a transaction into the transaction pool.
    *
    * @param txHash The hash of the transaction to add.
    * @param transaction The transaction to add.
    */
  def putTransactionToPool(txHash : Hash, transaction : Transaction) : Unit = {
    keyValueDB.putPrefixedObject(TRANSACTION_POOL, DUMMY_PREFIX_KEY, txHash, transaction )
  }

  /** Get a transaction from the transaction pool.
    *
    * @param txHash The hash of the transaction to get.
    * @return The transaction which matches the given transaction hash.
    */
  def getTransactionFromPool(txHash : Hash) : Option[Transaction] = {
    keyValueDB.getPrefixedObject(TRANSACTION_POOL, DUMMY_PREFIX_KEY, txHash)(HashCodec, TransactionCodec)
  }


  /** Get all transactions in the pool.
    *
    * @return List of transactions in the pool. List of (transaction hash, transaction) pair.
    */
  def getTransactionsFromPool() : List[(Hash, Transaction)] = {
    (
      using(keyValueDB.seekPrefixedObject(TRANSACTION_POOL, DUMMY_PREFIX_KEY)(HashCodec, TransactionCodec)) in {
        _.toList
      }
    ).map{ case (CStringPrefixed(_, txHash), transaction ) => (txHash, transaction) }
  }

  /** Del a transaction from the pool.
    *
    * @param txHash The hash of the transaction to remove.
    */
  def delTransactionFromPool(txHash : Hash) : Unit = {
    keyValueDB.delPrefixedObject(TRANSACTION_POOL, DUMMY_PREFIX_KEY, txHash )
  }

}

