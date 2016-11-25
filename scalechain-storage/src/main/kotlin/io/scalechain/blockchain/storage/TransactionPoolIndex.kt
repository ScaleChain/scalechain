package io.scalechain.blockchain.storage

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.proto.codec.{TransactionPoolEntryCodec, TransactionCodec, OneByteCodec, HashCodec}
import io.scalechain.blockchain.proto.{TransactionPoolEntry, Transaction, OneByte, Hash}
import io.scalechain.blockchain.storage.index.DatabaseTablePrefixes._
import io.scalechain.blockchain.storage.index.{KeyValueDatabase, DatabaseTablePrefixes}
import io.scalechain.util.HexUtil._
import io.scalechain.util.Using._
import org.slf4j.LoggerFactory

object TransactionPoolIndex {
  // A dummy prefix key to list all transactions in the disk-pool.
  val DUMMY_PREFIX_KEY = "0"

}

/**
  * Provides index operations for disk-pool, which keeps transactions on-disk instead of mempool.
  * c.f. Orphan transactions are not stored in the disk-pool.
  */
trait TransactionPoolIndex {
  private val logger = LoggerFactory.getLogger(TransactionPoolIndex.javaClass)

  import TransactionPoolIndex._
  import DatabaseTablePrefixes._
  private implicit val hashCodec = HashCodec
  private implicit val transactionCodec = TransactionPoolEntryCodec
  protected val PoolIndexPrefix = TRANSACTION_POOL

  /** Put a transaction into the transaction pool.
    *
    * @param txHash The hash of the transaction to add.
    * @param transactionPoolEntry The transaction to add.
    */
  fun putTransactionToPool(txHash : Hash, transactionPoolEntry : TransactionPoolEntry)(implicit db : KeyValueDatabase) : Unit {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")

    db.putPrefixedObject(PoolIndexPrefix, DUMMY_PREFIX_KEY, txHash, transactionPoolEntry )
  }

  /** Get a transaction from the transaction pool.
    *
    * @param txHash The hash of the transaction to get.
    * @return The transaction which matches the given transaction hash.
    */
  fun getTransactionFromPool(txHash : Hash)(implicit db : KeyValueDatabase) : Option<TransactionPoolEntry> {
    //logger.trace(s"getTransactionFromPool : ${txHash}")

    db.getPrefixedObject(PoolIndexPrefix, DUMMY_PREFIX_KEY, txHash)(HashCodec, TransactionPoolEntryCodec)
  }


  /** Get all transactions in the pool.
    *
    * @return List of transactions in the pool. List of (transaction hash, transaction) pair.
    */
  fun getTransactionsFromPool()(implicit db : KeyValueDatabase) : List<(Hash, TransactionPoolEntry)> {
    (
      using(db.seekPrefixedObject(PoolIndexPrefix, DUMMY_PREFIX_KEY)(HashCodec, TransactionPoolEntryCodec)) in {
        _.toList
      }
    ).map{ case (CStringPrefixed(_, txHash), transactionPoolEntry ) => (txHash, transactionPoolEntry) }
  }

  /** Del a transaction from the pool.
    *
    * @param txHash The hash of the transaction to remove.
    */
  fun delTransactionFromPool(txHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    //logger.trace(s"delTransactionFromPool : ${txHash}")

    db.delPrefixedObject(PoolIndexPrefix, DUMMY_PREFIX_KEY, txHash )
  }

}

