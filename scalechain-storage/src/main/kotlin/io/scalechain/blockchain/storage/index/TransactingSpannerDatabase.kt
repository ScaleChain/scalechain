package io.scalechain.blockchain.storage.index

import com.google.cloud.spanner.*
import io.scalechain.util.Bytes
import com.google.cloud.spanner.TransactionContext
import com.google.cloud.spanner.TransactionRunner.TransactionCallable
import io.scalechain.util.ArrayUtil
import java.util.*

class IteratorMerger(val dbIterator : ClosableIterator<Pair<ByteArray,ByteArray>>, val keyCache : TreeMap<Bytes, ByteArray>) {
  val keyCacheIterator = keyCache.iterator()

  // prefetched db key/value
  var dbKeyValue: Pair<ByteArray,ByteArray>? = null

  // prefetched key/value in the keyCache
  var cachedKeyValue: Pair<ByteArray,ByteArray>? = null

  fun next() : Pair<ByteArray,ByteArray> {
    // Step 1 : prefetch next key for each iterator
    if (dbKeyValue == null && dbIterator.hasNext())
      dbKeyValue = dbIterator.next()

    if (cachedKeyValue == null && keyCacheIterator.hasNext()) {
      val nextKeyValue = keyCacheIterator.next()
      cachedKeyValue = Pair(nextKeyValue.key.array, nextKeyValue.value)
    }

    // Step 2 : Sort merge the two prefetched keys.
    if (dbKeyValue == null) {
      assert(cachedKeyValue != null)
      return cachedKeyValue!!
    }

    if (cachedKeyValue == null) {
      assert(dbKeyValue != null)
      return dbKeyValue!!
    }

    assert( dbKeyValue != null && cachedKeyValue != null)

    val dbKey = dbKeyValue!!.first
    val cachedKey = cachedKeyValue!!.first
    val compareResult = ArrayUtil.compare( dbKey, cachedKey)

    if (compareResult == 0) { // Two keys are identical. prefer the cached Key.
      try {
        return cachedKeyValue!!
      } finally {
        dbKeyValue = null
        cachedKeyValue = null
      }
    } else if (compareResult < 0){ // dbKey is less than the cached Key. use the dbKey only.
      try {
        return dbKeyValue!!
      } finally {
        dbKeyValue = null
      }
    } else { // cached key is less than the db key. use cached key only.
      try {
        return cachedKeyValue!!
      } finally {
        cachedKeyValue = null
      }
    }
  }

  fun hasNext() : Boolean {
    return dbIterator.hasNext() || keyCacheIterator.hasNext()
  }
}


/**
 * An iterator that merges actual key/value iterator with volatile key/value cache which has keys and values that are not committed.
 */
class TransactingSpannerDatabaseIterator(val iterator : ClosableIterator<Pair<ByteArray,ByteArray>>, putCache : TreeMap<Bytes, ByteArray>, val delCache : HashMap<Bytes, Unit>) : ClosableIterator<Pair<ByteArray,ByteArray>> {
  val merger = IteratorMerger(iterator, putCache)

  var prefetchedKeyValue: Pair<ByteArray,ByteArray>? = null
  private var isClosed = false

  /**
   * Return the next key/value pair.
   * Assumption : next() is called only after checking if hasNext() returns true.
   */
  override fun next() : Pair<ByteArray,ByteArray> {
    assert( !isClosed )

    if (!hasNext()) { // We should have a next key. cachedNextKey is set in hasNext method.
      throw AssertionError()
    }

    assert(prefetchedKeyValue != null)

    val rawKey = prefetchedKeyValue!!.first
    val rawValue = prefetchedKeyValue!!.second

    prefetchedKeyValue = null

    return Pair(rawKey, rawValue)
  }

  /**
   * Check if we have next key/value pair to get.
   */
  override fun hasNext() : Boolean {
    if (isClosed) {
      return false
    } else {
      if (prefetchedKeyValue != null) { // If we have a cached next key, return true.
        return true
      }
      prefetchedKeyValue = prefetchNext()
      return prefetchedKeyValue !=null
    }
  }

  /**
   * Prefetch next key checking if the key was deleted.
   */
  tailrec fun prefetchNext() : Pair<ByteArray,ByteArray>? {
    assert( prefetchedKeyValue == null )
    if (merger.hasNext()) {
      val nextKeyValue = merger.next()
      val nextKey = nextKeyValue.first
      // If it is a deleted key, we don't have the next item.
      if ( delCache.containsKey(Bytes(nextKey)) ) {
        return prefetchNext()
      } else {
        return nextKeyValue
      }
    } else {
      return null
    }
  }

  override fun close() : Unit {
    iterator.close()
    isClosed = true
    prefetchedKeyValue = null
  }
}

// Imports the Google Cloud client library

/**
 * RocksDB with transaction support. This class is not thread-safe.
 */
class TransactingSpannerDatabase(private val db : SpannerDatabase) : TransactingKeyValueDatabase {
  private var putCache : TreeMap<Bytes, ByteArray>? = null // key, value
  private var delCache : HashMap<Bytes, Unit>? = null // key, dummy

  /**
   * Begin a database transaction.
   */
  override fun beginTransaction() : Unit {
    assert(putCache == null)

    putCache = TreeMap<Bytes, ByteArray>()
    delCache = HashMap<Bytes, Unit>()
  }

  /**
   * Commit the database transaction began.
   */
  override fun commitTransaction() : Unit {
    assert(putCache != null)
    assert(delCache != null)

    db.getDbClient()
      .readWriteTransaction()
      .run(
        object : TransactionCallable<Void> {
          override fun run(transaction : TransactionContext) : Void? {

            // Put all keys into the transaction from our putCache.
            putCache!!.forEach { pair ->
              transaction.buffer(
                Mutation.newInsertOrUpdateBuilder(db.tableName)
                  .set("key")
                  .to(com.google.cloud.ByteArray.copyFrom(pair.key.array))
                  .set("value")
                  .to(com.google.cloud.ByteArray.copyFrom(pair.value))
                  .build())
            }

            delCache!!.forEach { pair ->
              transaction.buffer(
                Mutation.delete(db.tableName, SpannerDatabase.toKey(pair.key.array))
              )
            }

            return null
          }
        })

    putCache = null
    delCache = null
  }

  /**
   * Abort the database transaction began.
   */
  override fun abortTransaction() : Unit {
    assert(putCache != null)
    putCache = null
    delCache = null
  }


  override fun seek(keyOption : ByteArray? ) : ClosableIterator<Pair<ByteArray, ByteArray>> {
    return TransactingSpannerDatabaseIterator(db.seek(keyOption), putCache!!, delCache!!)
  }

  override fun get(key : ByteArray ) : ByteArray? {
    if (delCache == null || putCache == null) {
      return db.get(key)
    } else {
      if (delCache!!.contains(Bytes(key))) {
        return null
      } else {
        val value = putCache!!.get(Bytes(key))
        if (value != null) {
          return value
        } else {
          return db.get(key)
        }
      }
    }
  }

  override fun put(key : ByteArray, value : ByteArray ) : Unit {
    putCache!!.put(Bytes(key), value)
    delCache!!.remove(Bytes(key))
  }

  override fun del(key : ByteArray) : Unit {
    delCache!!.put(Bytes(key), Unit)
    putCache!!.remove(Bytes(key))
  }

  @Deprecated("TransactingRocksDatabase.transacting should never be called. transacting method can be called from a non-transactional RocksDatabase only.", ReplaceWith(""), DeprecationLevel.ERROR)
  override fun transacting(): TransactingKeyValueDatabase {
    throw AssertionError()
  }

  @Deprecated("TransactingRocksDatabase.close should never be called. close method can bel called from a non-transactional RocksDatabase only.", ReplaceWith(""), DeprecationLevel.ERROR)
  override fun close() : Unit {
    throw AssertionError()
  }
}