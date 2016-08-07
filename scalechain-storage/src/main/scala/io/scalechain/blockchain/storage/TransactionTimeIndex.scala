package io.scalechain.blockchain.storage

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.proto.codec.{LongValueCodec, OneByteCodec, TransactionPoolEntryCodec, HashCodec}
import io.scalechain.blockchain.proto.{LongValue, OneByte, TransactionPoolEntry, Hash}
import io.scalechain.blockchain.storage.index.DatabaseTablePrefixes._
import io.scalechain.blockchain.storage.index.{DatabaseTablePrefixes, KeyValueDatabase}
import io.scalechain.util.Base58Util
import io.scalechain.util.Using._
import org.slf4j.LoggerFactory

object TransactionTimeIndex {
  val maxBase58EncodedLength = Base58Util.encode( LongValueCodec.serialize( LongValue( Long.MaxValue ) ) ).length

  def timeToString(nanoSeconds : Long) : String = {
    val encodedString = Base58Util.encode( LongValueCodec.serialize( LongValue( nanoSeconds) ) )
    if (encodedString.length > maxBase58EncodedLength ) {
      assert(false)
      ""
    } else if (encodedString.length == maxBase58EncodedLength ) {
      encodedString
    } else {
      // Prefix the base58 encoded string with "1", to make the encoded string take the maxBase58EncodedLength bytes.
      // This is necessary to sort transactions by transaction time.
      val prefix = "1" * (maxBase58EncodedLength - encodedString.length)
      prefix + encodedString
    }
  }
}
/**
  * Maintains an index from the creation time of a transaction to the transaction hash.
  */
trait TransactionTimeIndex {
  private val logger = Logger( LoggerFactory.getLogger(classOf[TransactionTimeIndex]) )

  import TransactionTimeIndex._
  import DatabaseTablePrefixes._
  private implicit val hashCodec = HashCodec
  private implicit val oneByteCodec = OneByteCodec

  def putTransactionTime(creationTime : Long, txHash : Hash)(implicit db : KeyValueDatabase) : Unit = {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")

    val keyPrefix = timeToString(creationTime)

    db.putPrefixedObject(TRANSACTION_TIME, keyPrefix, txHash, OneByte('\0') )
  }

  /** Get a transaction from the transaction pool.
    *
    * @param count The number of hashes to get.
    * @return The transaction which matches the given transaction hash.
    */
  def getOldestTransactionHashes(count : Int)(implicit db : KeyValueDatabase) : List[CStringPrefixed[Hash]] = {
    assert(count > 0)
    //logger.trace(s"getTransactionFromPool : ${txHash}")

    using( db.seekPrefixedObject(TRANSACTION_TIME)(HashCodec, OneByteCodec) ) in {
      _.take(count) // Take count elements from the iterator.
       .map(_._1) // Do not use the dummy byte value, but keep StringPrefixed transaction hash only.
       .toList
    }
  }

  /** Del a transaction from the pool.
    *
    * @param key The string prefixed hash to remove. The prefixed string contains the creation time of the transaction.
    */
  def delTransactionTime(key : CStringPrefixed[Hash])(implicit db : KeyValueDatabase) : Unit = {

    db.delPrefixedObject(TRANSACTION_TIME, key )
  }
}