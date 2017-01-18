package io.scalechain.blockchain.storage.index

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.TransactionPoolEntryCodec
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.OneByteCodec
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.proto.TransactionPoolEntry
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.index.DB
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.util.HexUtil
//import io.scalechain.util.Using.*
import org.slf4j.LoggerFactory

/**
  * Provides index operations for disk-pool, which keeps transactions on-disk instead of mempool.
  * c.f. Orphan transactions are not stored in the disk-pool.
  */
interface TransactionPoolIndex {
  //private val logger = LoggerFactory.getLogger(TransactionPoolIndex::class.java)
  fun getTxPoolPrefix() : Byte = DB.TRANSACTION_POOL

  /** Put a transaction into the transaction pool.
    *
    * @param txHash The hash of the transaction to add.
    * @param transactionPoolEntry The transaction to add.
    */
  fun putTransactionToPool(db : KeyValueDatabase, txHash : Hash, transactionPoolEntry : TransactionPoolEntry) : Unit {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")
    //println("putTransactionToPool ${txHash}")

    db.putPrefixedObject(HashCodec, TransactionPoolEntryCodec, getTxPoolPrefix(), DUMMY_PREFIX_KEY, txHash, transactionPoolEntry )

    // BUGBUG : Remove to improve performance
    //assert( getTransactionFromPool(db, txHash) != null)
  }

  /** Get a transaction from the transaction pool.
    *
    * @param txHash The hash of the transaction to get.
    * @return The transaction which matches the given transaction hash.
    */
  fun getTransactionFromPool(db : KeyValueDatabase, txHash : Hash) : TransactionPoolEntry? {
    //logger.trace(s"getTransactionFromPool : ${txHash}")

    return db.getPrefixedObject(HashCodec, TransactionPoolEntryCodec, getTxPoolPrefix(), DUMMY_PREFIX_KEY, txHash)
  }


  /** Get all transactions in the pool.
    *
    * @return List of transactions in the pool. List of (transaction hash, transaction) pair.
    */
  fun getTransactionsFromPool(db : KeyValueDatabase) : List<Pair<Hash, TransactionPoolEntry>> {
    val iterator = db.seekPrefixedObject(HashCodec, TransactionPoolEntryCodec, getTxPoolPrefix(), DUMMY_PREFIX_KEY)
    try {
      return iterator.asSequence().toList().map { prefixedObject ->
        val cstringPrefixed = prefixedObject.first
        val txPoolEntry = prefixedObject.second
        val hash = cstringPrefixed.data
        Pair(hash, txPoolEntry)
      }
    } finally {
      iterator.close()
    }
  }

  /** Del a transaction from the pool.
    *
    * @param txHash The hash of the transaction to remove.
    */
  fun delTransactionFromPool(db : KeyValueDatabase, txHash : Hash) : Unit {
    //println("delTransactionToPool ${txHash}")
    //logger.trace(s"delTransactionFromPool : ${txHash}")

    db.delPrefixedObject(HashCodec, getTxPoolPrefix(), DUMMY_PREFIX_KEY, txHash )

    // BUGBUG : Remove to improve performance
    //assert( getTransactionFromPool(db, txHash) == null)
  }

  companion object {
    // A dummy prefix key to list all transactions in the disk-pool.
    val DUMMY_PREFIX_KEY = "0"
  }
}

