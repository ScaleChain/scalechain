package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.rocksdb._
;

/**
  * Created by kangmo on 3/11/16.
  */
class RocksDatabase(path : File, prefixSizeOption: Option[Int] = None) extends KeyValueDatabase {
  assert( Storage.initialized )

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val defaultOptions = new Options().setCreateIfMissing(true);
  private val options = prefixSizeOption match {
    case Some(prefixSize) => defaultOptions.useFixedLengthPrefixExtractor(prefixSize)
    case _ => defaultOptions
  }
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

  def iterator(cf: ColumnFamilyHandle): RocksIterator = {
    db.newIterator(cf)
  }

  def close() = {
    db.close
  }
}
