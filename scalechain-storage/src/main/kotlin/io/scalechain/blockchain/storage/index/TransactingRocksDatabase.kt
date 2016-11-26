package io.scalechain.blockchain.storage.index

import java.io.File
import java.nio.ByteBuffer

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.util.HexUtil
import io.scalechain.util.ArrayUtil
//import io.scalechain.util.Using.
import org.rocksdb.RocksDB
import org.rocksdb.WriteOptions
import org.rocksdb.WriteBatchWithIndex

/**
  * RocksDB with transaction support. This class is not thread-safe.
  */
class TransactingRocksDatabase(private val db : RocksDatabase) : KeyValueDatabase {

  var writeBatch : WriteBatchWithIndex? = null

  var putCache : MutableMap<ByteArray, ByteArray>? = null // key, value
  var delCache : MutableMap<ByteArray, Unit>? = null // key, dummy

  /**
    * Begin a database transaction.
    */
  fun beginTransaction() : Unit {
    assert(writeBatch == null)
    writeBatch = WriteBatchWithIndex(true)
    putCache = mutableMapOf<ByteArray, ByteArray>()
    delCache = mutableMapOf<ByteArray, Unit>()
  }

  /**
    * Commit the database transaction began.
    */
  fun commitTransaction() : Unit {
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
    // BUGBUG : Need to set to true?
    writeOptions.setSync(false)
    //writeOptions.setDisableWAL(true)

    db.db!!.write(writeOptions, writeBatch)

    writeBatch = null
    putCache = null
    delCache = null
  }

  /**
    * Abort the database transaction began.
    */
  fun abortTransaction() : Unit {
    assert(writeBatch != null)
//    println(s"Aborting a transaction. Write count : ${writeBatch.count}")
    writeBatch = null
    putCache = null
    delCache = null
  }


  override fun seek(keyOption : ByteArray? ) : ClosableIterator<Pair<ByteArray, ByteArray>> {
    val rocksIterator =
      if (writeBatch!= null)
        writeBatch!!.newIteratorWithBase( db.db!!.newIterator() )
      else
        db.db!!.newIterator()

    return db.seek(rocksIterator, keyOption)
  }

  override fun get(key : ByteArray ) : ByteArray? {
    if (delCache == null || putCache == null) {
      return db.get(key)
    } else {
      if (delCache!!.contains(key)) {
        return null
      } else {
        val value = putCache!!.get(key)
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
        using(seek(Some(key))) in { it =>
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

    putCache!!.put(key, value)
    delCache!!.remove(key)
  }

  override fun del(key : ByteArray) : Unit {
//    println(s"del ${HexUtil.hex(key)}")
    assert(writeBatch != null)
    writeBatch!!.remove(key)

    delCache!!.put(key, Unit)
    putCache!!.remove(key)
  }

  override fun close() : Unit {
    db.close()
  }
}