package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.storage.Storage
import org.rocksdb.{RocksIterator, Options, RocksDB}

/**
  * A KeyValueDatabase implementation using RocksDB.
  */
class RocksDatabase(path : File) extends KeyValueDatabase {
  assert( Storage.initialized )

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val options = new Options().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
  private val db = RocksDB.open(options, path.getAbsolutePath)


  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  def seek(keyOption : Option[Array[Byte]] ) : ClosableIterator[(Array[Byte], Array[Byte])] = {
    class KeyValueIterator(rocksIterator : RocksIterator) extends ClosableIterator[(Array[Byte],Array[Byte])] {
      var isClosed = false
      def next : (Array[Byte],Array[Byte]) = {
        assert( !isClosed )

        if (!rocksIterator.isValid) {
            throw new GeneralException(ErrorCode.NoMoreKeys)
        }

        val rawKey = rocksIterator.key
        val rawValue = rocksIterator.value

        rocksIterator.next

        (rawKey, rawValue)
      }
      def hasNext : Boolean = {
        if (isClosed) {
          false
        } else {
          rocksIterator.isValid
        }
      }

      def close : Unit = {
        rocksIterator.dispose()
        isClosed = true
      }
    }
    val rocksIterator =  db.newIterator()

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
    db.remove(key)
  }

  def close() : Unit = {
    db.close
    options.dispose
  }
}
