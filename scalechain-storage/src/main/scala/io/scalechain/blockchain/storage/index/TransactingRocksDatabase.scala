package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.util.{HexUtil, ArrayUtil}
import io.scalechain.util.Using._
import org.rocksdb.{WriteOptions, WriteBatchWithIndex}

/**
  * Created by kangmo on 7/9/16.
  */
class TransactingRocksDatabase(path: File) extends RocksDatabase(path) {
  var writeBatch : WriteBatchWithIndex = null

  /**
    * Begin a database transaction.
    */
  def beginTransaction() : Unit = {
    assert(writeBatch == null)
    writeBatch = new WriteBatchWithIndex(true)
  }

  /**
    * Commit the database transaction began.
    */
  def commitTransaction() : Unit = {
    assert(writeBatch != null)
    val writeOptions = new WriteOptions()
    // BUGBUG : Need to set to true?
    writeOptions.setSync(false)
    db.write(writeOptions, writeBatch)
    writeBatch = null
  }

  /**
    * Abort the database transaction began.
    */
  def abortTransaction() : Unit = {
    assert(writeBatch != null)
    writeBatch = null
  }

  override def seek(keyOption : Option[Array[Byte]] ) : ClosableIterator[(Array[Byte], Array[Byte])] = {
    val rocksIterator = writeBatch.newIteratorWithBase( db.newIterator() )
    super.seek(rocksIterator, keyOption)
  }

  override def get(key : Array[Byte] ) : Option[Array[Byte]] = {
    if ( writeBatch.count() == 0 ) {
      super.get(key)
    } else {
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
  }

  override def put(key : Array[Byte], value : Array[Byte] ) : Unit = {
//    println(s"put ${HexUtil.hex(key)}, ${HexUtil.hex(value)}")
    assert(writeBatch != null)
    writeBatch.put(key, value)
  }

  override def del(key : Array[Byte]) : Unit = {
//    println(s"del ${HexUtil.hex(key)}")
    assert(writeBatch != null)
    writeBatch.remove(key)
  }
}