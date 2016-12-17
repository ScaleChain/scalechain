package io.scalechain.blockchain.storage.index


import org.iq80.leveldb.*
import org.fusesource.leveldbjni.JniDBFactory.*
import java.io.*

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.storage.Storage


/**
  * A KeyValueDatabase implementation using LevelDB.
  */
@Deprecated("LevelDatabase does not support transaction feature", ReplaceWith("RocksDatabase"), DeprecationLevel.ERROR)
class LevelDatabase(path : File) : KeyValueDatabase {

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private var options : Options? = Options()

  private var db = factory.open(path, options )

  fun getDb() = db

  init {
    assert( Storage.initialized() )
    options!!.createIfMissing(true)
  }

  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  override fun seek(keyOption : ByteArray? ) : ClosableIterator<Pair<ByteArray, ByteArray>> {
    class KeyValueIterator(private val iterator : DBIterator) : ClosableIterator<Pair<ByteArray,ByteArray>> {
      override fun next() : Pair<ByteArray,ByteArray> {
        if (!iterator.hasNext()) {
          throw GeneralException(ErrorCode.NoMoreKeys)
        }

        val nextKeyValue : Map.Entry<ByteArray, ByteArray> = iterator.peekNext()
        iterator.next()
        return Pair(nextKeyValue.key, nextKeyValue.value)
      }
      override fun hasNext() : Boolean = iterator.hasNext()

      override fun close() = iterator.close()
    }

    val levelDBIterator =  db.iterator()

    if (keyOption != null) {
      levelDBIterator.seek(keyOption)
    } else {
      levelDBIterator.seekToFirst()
    }

    return KeyValueIterator(levelDBIterator)
  }


  override fun get(key : ByteArray ) : ByteArray? {
    return db.get(key)
  }

  override fun put(key : ByteArray, value : ByteArray ) : Unit {
    db.put(key, value)
  }

  override fun del(key : ByteArray) : Unit {
    db.delete(key)
  }

  /**
   * Create a new transacting db that supports transaction commit/abort operations.
   */
  override fun transacting() : TransactingKeyValueDatabase {
    // transacting leveldb is not supported, because it lacks Transactions and WriteBatchWithIndex
    // See : https://github.com/facebook/rocksdb/wiki/Features-Not-in-LevelDB

    // If we can implement it, we have a pure-java version of KeyValueDatabase and TransactingKeyValueDatabase.
    // It measn that we can quickly run our program without building rocksdb for a specific platform.
    throw AssertionError()
  }


  override fun close() : Unit {
    if (db != null) {
      db!!.close()
    }

    db = null
    options = null
  }
}
