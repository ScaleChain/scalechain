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

import scala.collection.mutable.ListBuffer

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

  protected val TimeIndexPrefix = TRANSACTION_TIME
  /** Put a transaction into the transaction time index.
    *
    * @param creationTime The time when the transaction was created (in nano seconds)
    * @param txHash The hash of the transaction to add
    */
  def putTransactionTime(creationTime : Long, txHash : Hash)(implicit db : KeyValueDatabase) : Unit = {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")

    val keyPrefix = timeToString(creationTime)

    db.putPrefixedObject(TimeIndexPrefix, keyPrefix, txHash, OneByte('\0') )
  }

  /** Get a transaction from the transaction pool.
    *
    * @param count The number of hashes to get.
    * @return The transaction which matches the given transaction hash.
    */
  def getOldestTransactionHashes(count : Int)(implicit db : KeyValueDatabase) : List[CStringPrefixed[Hash]] = {
    assert(count > 0)
    //logger.trace(s"getTransactionFromPool : ${txHash}")

    using( db.seekPrefixedObject(TimeIndexPrefix)(HashCodec, OneByteCodec) ) in {
      iter =>
        val buffer = new ListBuffer[CStringPrefixed[Hash]]
        var copied = 0
        while(copied < count && iter.hasNext) {
          val (key, _) = iter.next()
          buffer.append(key)
          copied += 1
        }
        buffer.toList
    }
  }

  /** Del a transaction from the pool.
    *
    * @param creationTime The time when the transaction was created (in nano seconds)
    * @param txHash The hash of the transaction to remove
    */
  def delTransactionTime(creationTime: Long, txHash : Hash)(implicit db : KeyValueDatabase) : Unit = {

    val keyPrefix = timeToString(creationTime)

    db.delPrefixedObject(TimeIndexPrefix, keyPrefix, txHash )
  }
}