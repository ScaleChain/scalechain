package io.scalechain.blockchain.storage.index


import java.io.File
import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import java.io._

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.storage.Storage


/**
  * A KeyValueDatabase implementation using LevelDB.
  */
class LevelDatabase(path : File) : KeyValueDatabase {
  assert( Storage.initialized )

  fun beginTransaction() : Unit {
    // No transaction supported. do nothing.
  }
  fun commitTransaction() : Unit {
    // No transaction supported. do nothing.
  }
  fun abortTransaction() : Unit {
    // No transaction supported. do nothing.
  }

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val options = Options()
  options.createIfMissing(true)

  private val db = factory.open(path, options )


  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  fun seek(keyOption : Option<Array<Byte>> ) : ClosableIterator<(ByteArray, ByteArray)> {
    class KeyValueIterator(iterator : DBIterator) : ClosableIterator<(ByteArray,ByteArray)> {
      fun next : (ByteArray,ByteArray) {
        if (!iterator.hasNext) {
          throw GeneralException(ErrorCode.NoMoreKeys)
        }

        val nextKeyValue = iterator.peekNext()
        iterator.next
        (nextKeyValue.getKey, nextKeyValue.getValue)
      }
      fun hasNext : Boolean = iterator.hasNext

      fun close : Unit {
        iterator.close()
      }
    }

    val rocksIterator =  db.iterator()

    if (keyOption.isDefined) {
      rocksIterator.seek(keyOption.get)
    } else {
      rocksIterator.seekToFirst()
    }

    KeyValueIterator(rocksIterator)
  }


  fun get(key : ByteArray ) : Option<Array<Byte>> {
    val value = db.get(key)
    if ( value != null )
      Some(value)
    else None
  }

  fun put(key : ByteArray, value : ByteArray ) : Unit {
    db.put(key, value)
  }

  fun del(key : ByteArray) : Unit {
    db.delete(key)
  }

  fun close() : Unit {
    db.close
  }
}
