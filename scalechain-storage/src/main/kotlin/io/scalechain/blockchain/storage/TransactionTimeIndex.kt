package io.scalechain.blockchain.storage

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.LongValueCodec
import io.scalechain.blockchain.proto.codec.OneByteCodec
import io.scalechain.blockchain.proto.codec.TransactionPoolEntryCodec
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.proto.LongValue
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.TransactionPoolEntry
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.index.DB
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.util.Base58Util
//import io.scalechain.util.Using._
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

/**
  * Maintains an index from the creation time of a transaction to the transaction hash.
  */
interface TransactionTimeIndex {
  //private val logger = LoggerFactory.getLogger(TransactionTimeIndex::class.java)

  /** Put a transaction into the transaction time index.
    *
    * @param creationTime The time when the transaction was created (in nano seconds)
    * @param txHash The hash of the transaction to add
    */
  fun putTransactionTime(db : KeyValueDatabase, creationTime : Long, txHash : Hash) : Unit {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")

    val keyPrefix = timeToString(creationTime)

    db.putPrefixedObject(HashCodec, OneByteCodec, DB.TRANSACTION_TIME, keyPrefix, txHash, OneByte(0.toByte()) )
  }

  /** Get a transaction from the transaction pool.
    *
    * @param count The number of hashes to get.
    * @return The transaction which matches the given transaction hash.
    */
  fun getOldestTransactionHashes(db : KeyValueDatabase, count : Int) : List<CStringPrefixed<Hash>> {
    assert(count > 0)
    //logger.trace(s"getTransactionFromPool : ${txHash}")
    val iterator = db.seekPrefixedObject(HashCodec, OneByteCodec, DB.TRANSACTION_TIME)
    try {
      val buffer = arrayListOf<CStringPrefixed<Hash>>()
      var copied = 0
      while(copied < count && iterator.hasNext()) {
        val (key, value) = iterator.next()
        buffer.add(key)
        copied += 1
      }
      return buffer.toList()
    } finally {
      iterator.close()
    }
  }

  /** Del a transaction from the pool.
    *
    * @param creationTime The time when the transaction was created (in nano seconds)
    * @param txHash The hash of the transaction to remove
    */
  fun delTransactionTime(db : KeyValueDatabase, creationTime: Long, txHash : Hash) : Unit {

    val keyPrefix = timeToString(creationTime)

    db.delPrefixedObject(HashCodec, DB.TRANSACTION_TIME, keyPrefix, txHash )
  }

  fun delTransactionTime(db : KeyValueDatabase, key : CStringPrefixed<Hash>) : Unit {
    db.delPrefixedObject(HashCodec, DB.TRANSACTION_TIME, key )
  }

  companion object {
    val MaxBase58EncodedLength = Base58Util.encode( LongValueCodec.encode( LongValue( Long.MAX_VALUE ) ) ).length

    fun timeToString(nanoSeconds : Long) : String {
      val encodedString = Base58Util.encode( LongValueCodec.encode( LongValue( nanoSeconds) ) )
      if (encodedString.length > MaxBase58EncodedLength) {
        assert(false)
        return ""
      } else if (encodedString.length == MaxBase58EncodedLength) {
        return encodedString
      } else {
        // Prefix the base58 encoded string with "1", to make the encoded string take the MaxBase58EncodedLength bytes.
        // This is necessary to sort transactions by transaction time.
        val prefix = ByteArray(MaxBase58EncodedLength - encodedString.length, {'1'.toByte()}).toString()
        return prefix + encodedString
      }
    }
  }
}