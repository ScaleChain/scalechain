package io.scalechain.blockchain.storage.index

import java.io.File
import java.util
import io.scalechain.blockchain.storage.Storage
import org.rocksdb._

/**
  * Created by kangmo on 3/11/16.
  */
class RocksDatabase(path : File) extends KeyValueDatabase {
  assert( Storage.initialized )

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val options = new Options().setCreateIfMissing(true);
  private var db = RocksDB.open(options, path.getAbsolutePath)

  private val columnFamilyDescriptors = util.Arrays.asList(
    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions())
  )
  private val columnFamilyHandles = new util.ArrayList[ColumnFamilyHandle]()

  def openWithColumnFamily = {

    // first, close db instance if an instance is already exist
    close()
    db = RocksDB.open(new DBOptions(), path.getAbsolutePath, columnFamilyDescriptors, columnFamilyHandles)
  }

  def createColumnFamily(descriptor: String) = {

    // create new column family descriptor and new column family handler
    val columnFamilyHandle = db.createColumnFamily(
      new ColumnFamilyDescriptor(descriptor.getBytes(),
      new ColumnFamilyOptions())
    )

    columnFamilyHandles.add(columnFamilyHandle)
  }

  def listColumnFamilies(): util.List[Array[Byte]] = {

    // listColumnFamilies is static function
    val columnFamilyNames = RocksDB.listColumnFamilies(options, path.getAbsolutePath)
    columnFamilyNames
  }

  def dropColumnFamily(index: Int) = {

    db.dropColumnFamily(columnFamilyHandles.get(index))
  }

  def getColumnFamilyIndex(descriptor: String): Int = {

    val columnFamilyNames = listColumnFamilies()
    var index = -1

    for(i <- 0 until columnFamilyNames.size(); if index == -1) {
      if(descriptor.equals( new String(columnFamilyNames.get(i)))) {
        index = i
      }
    }

    index
  }

  def getColumnFamilyHandle(descriptor: String): ColumnFamilyHandle = {

    val index = getColumnFamilyIndex(descriptor)
    columnFamilyHandles.get(index)
  }

  def get(key : Array[Byte] ) : Option[Array[Byte]] = {
    val value = db.get(key)
    if ( value != null )
      Some(value)
    else None
  }

  def get(cf: ColumnFamilyHandle, key: Array[Byte]): Option[Array[Byte]] = {
    val value = db.get(cf, key)
    if ( value != null )
      Some(value)
    else None
  }

  def put(key : Array[Byte], value : Array[Byte] ) : Unit = {
    db.put(key, value)
  }

  def put(cf: ColumnFamilyHandle, key: Array[Byte], value: Array[Byte]) : Unit = {
    db.put(cf, key, value)
  }

  def del(key : Array[Byte]) : Unit = {
    db.remove(key)
  }

  def close() = {
    db.close
  }
}
