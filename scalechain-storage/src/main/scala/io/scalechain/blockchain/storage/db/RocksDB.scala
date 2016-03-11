package io.scalechain.blockchain.storage.db

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.storage.Storage

import org.rocksdb.RocksDB;
import org.rocksdb.Options;

/**
  * Created by kangmo on 3/11/16.
  */
class RocksDatabase(path : String) extends KeyValueDatabase {
  assert( Storage.initialized )

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  val options = new Options().setCreateIfMissing(true);
  val db = RocksDB.open(options, path)

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
