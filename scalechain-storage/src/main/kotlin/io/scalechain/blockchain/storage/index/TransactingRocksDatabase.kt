package io.scalechain.blockchain.storage.index

import io.scalechain.util.Bytes
import org.rocksdb.WriteOptions
import org.rocksdb.WriteBatchWithIndex

/**
  * RocksDB with transaction support. This class is not thread-safe.
  */
class TransactingRocksDatabase(private val db : RocksDatabase) : TransactingKeyValueDatabase {

  private var writeBatch : WriteBatchWithIndex? = null

  private var putCache : MutableMap<Bytes, ByteArray>? = null // key, value
  private var delCache : MutableMap<Bytes, Unit>? = null // key, dummy

  /**
    * Begin a database transaction.
    */
  override fun beginTransaction() : Unit {
    assert(writeBatch == null)
    writeBatch = WriteBatchWithIndex(true)
    putCache = mutableMapOf<Bytes, ByteArray>()
    delCache = mutableMapOf<Bytes, Unit>()
  }

  /**
    * Commit the database transaction began.
    */
  override fun commitTransaction() : Unit {
    assert(writeBatch != null)
/*
    putCache foreach { case (key, value) =>
      writeBatch.put(key.array(), value)
    }
    delCache foreach { case (key, value) =>
      writeBatch.remove(key.array())
    }
*/
    //    println(s"Committing a transaction. Write count : ${writeBatch.count}")
    val writeOptions = WriteOptions()
    writeOptions.setSync(true)
    //writeOptions.setDisableWAL(true)

    db.getDb().write(writeOptions, writeBatch)

    writeBatch = null
    putCache = null
    delCache = null
  }

  /**
    * Abort the database transaction began.
    */
  override fun abortTransaction() : Unit {
    assert(writeBatch != null)
//    println(s"Aborting a transaction. Write count : ${writeBatch.count}")
    writeBatch = null
    putCache = null
    delCache = null
  }


  override fun seek(keyOption : ByteArray? ) : ClosableIterator<Pair<ByteArray, ByteArray>> {
    val rocksIterator =
      if (writeBatch!= null)
        writeBatch!!.newIteratorWithBase( db.getDb().newIterator() )
      else
        db.getDb().newIterator()

    return db.seek(rocksIterator, keyOption)
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

/*
    assert(writeBatch != null)

    {
      try {
        using(seek(Some(key))) { it =>
          val (searchedKey, searchedValue) = it.next()
          if (ArrayUtil.isEqual(key, searchedKey)) {
//            println(s"get ${HexUtil.hex(key)} => Some(${HexUtil.hex(searchedValue)})")
            Some(searchedValue)
          } else {
//            println(s"get ${HexUtil.hex(key)} => None")
            None
          }
        }
      } catch {
        case e : GeneralException => {
          if (e.code == ErrorCode.NoMoreKeys) { // No such key exists.
            None
          } else {
            throw e
          }
        }
      }
    }
*/
  }

  override fun put(key : ByteArray, value : ByteArray ) : Unit {
//    println(s"put ${HexUtil.hex(key)}, ${HexUtil.hex(value)}")
    assert(writeBatch != null)
    writeBatch!!.put(key, value)

    putCache!!.put(Bytes(key), value)
    delCache!!.remove(Bytes(key))
  }

  override fun del(key : ByteArray) : Unit {
//    println(s"del ${HexUtil.hex(key)}")
    assert(writeBatch != null)
    writeBatch!!.remove(key)

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