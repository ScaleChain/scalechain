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
class LevelDatabase(path : File) extends KeyValueDatabase {
  assert( Storage.initialized )

  def beginTransaction() : Unit = {
    // No transaction supported. do nothing.
  }
  def commitTransaction() : Unit = {
    // No transaction supported. do nothing.
  }
  def abortTransaction() : Unit = {
    // No transaction supported. do nothing.
  }

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val options = new Options()
  options.createIfMissing(true)

  private val db = factory.open(path, options )


  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  def seek(keyOption : Option[Array[Byte]] ) : ClosableIterator[(Array[Byte], Array[Byte])] = {
    class KeyValueIterator(iterator : DBIterator) extends ClosableIterator[(Array[Byte],Array[Byte])] {
      def next : (Array[Byte],Array[Byte]) = {
        if (!iterator.hasNext) {
          throw new GeneralException(ErrorCode.NoMoreKeys)
        }

        val nextKeyValue = iterator.peekNext()
        iterator.next
        (nextKeyValue.getKey, nextKeyValue.getValue)
      }
      def hasNext : Boolean = iterator.hasNext

      def close : Unit = {
        iterator.close()
      }
    }

    val rocksIterator =  db.iterator()

    if (keyOption.isDefined) {
      rocksIterator.seek(keyOption.get)
    } else {
      rocksIterator.seekToFirst()
    }

    new KeyValueIterator(rocksIterator)
  }


  def get(key : Array[Byte] ) : Option[Array[Byte]] = {
    val value = db.get(key)
    if ( value != null )
      Some(value)
    else None
  }

  def put(key : Array[Byte], value : Array[Byte] ) : Unit = {
    db.put(key, value)
  }

  def del(key : Array[Byte]) : Unit = {
    db.delete(key)
  }

  def close() : Unit = {
    db.close
  }
}
