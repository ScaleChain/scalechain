package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.rocksdb.{Options, RocksDB};

/**
  * Created by kangmo on 3/11/16.
  */
class RocksDatabase(path : File) extends KeyValueDatabase {
  assert( Storage.initialized )

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val options = new Options().setCreateIfMissing(true);
  private val db = RocksDB.open(options, path.getAbsolutePath)

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

  def close() = {
    db.close
  }
}
