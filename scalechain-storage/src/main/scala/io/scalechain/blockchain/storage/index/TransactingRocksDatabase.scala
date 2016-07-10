package io.scalechain.blockchain.storage.index

import java.io.File
import java.nio.ByteBuffer

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.util.{HexUtil, ArrayUtil}
import io.scalechain.util.Using._
import org.rocksdb.{RocksDB, WriteOptions, WriteBatchWithIndex}

/**
  * Created by kangmo on 7/9/16.
  */
class TransactingRocksDatabase(db : RocksDatabase) extends KeyValueDatabase {
  var writeBatch : WriteBatchWithIndex = null

  var putCache : scala.collection.mutable.Map[ByteBuffer, Array[Byte]] = null // key, value
  var delCache : scala.collection.mutable.Map[ByteBuffer, Unit] = null // key, dummy

  /**
    * Begin a database transaction.
    */
  def beginTransaction() : Unit = {
    assert(writeBatch == null)
    writeBatch = new WriteBatchWithIndex(true)
    putCache = scala.collection.mutable.Map[ByteBuffer, Array[Byte]]()
    delCache = scala.collection.mutable.Map[ByteBuffer, Unit]()
  }

  /**
    * Commit the database transaction began.
    */
  def commitTransaction() : Unit = {
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
    val writeOptions = new WriteOptions()
    // BUGBUG : Need to set to true?
    writeOptions.setSync(false)
    //writeOptions.setDisableWAL(true)
    db.db.write(writeOptions, writeBatch)
    writeBatch = null
    putCache = null
    delCache = null
  }

  /**
    * Abort the database transaction began.
    */
  def abortTransaction() : Unit = {
    assert(writeBatch != null)
//    println(s"Aborting a transaction. Write count : ${writeBatch.count}")
    writeBatch = null
    putCache = null
    delCache = null
  }

  def seek(keyOption : Option[Array[Byte]] ) : ClosableIterator[(Array[Byte], Array[Byte])] = {
    val rocksIterator =
      if (writeBatch!= null)
        writeBatch.newIteratorWithBase( db.db.newIterator() )
      else
        db.db.newIterator()

    db.seek(rocksIterator, keyOption)
  }

  def get(key : Array[Byte] ) : Option[Array[Byte]] = {
    val wrappedKey = ByteBuffer.wrap(key)
    if (delCache == null || putCache == null) {
      db.get(key)
    } else {
      if (delCache.contains(wrappedKey)) {
        None
      } else {
        val wrappedValue = putCache.get(wrappedKey)
        if (wrappedValue.isDefined) {
          wrappedValue
        } else {
          db.get(key)
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

  def put(key : Array[Byte], value : Array[Byte] ) : Unit = {
//    println(s"put ${HexUtil.hex(key)}, ${HexUtil.hex(value)}")
    assert(writeBatch != null)
    writeBatch.put(key, value)

    val wrappedKey = ByteBuffer.wrap(key)
    putCache.put(wrappedKey, value)
    delCache.remove(wrappedKey)
  }

  def del(key : Array[Byte]) : Unit = {
//    println(s"del ${HexUtil.hex(key)}")
    assert(writeBatch != null)
    writeBatch.remove(key)

    val wrappedKey = ByteBuffer.wrap(key)
    delCache.put(wrappedKey, ())
    putCache.remove(wrappedKey)
  }

  def close() : Unit = {
    db.close()
  }
}