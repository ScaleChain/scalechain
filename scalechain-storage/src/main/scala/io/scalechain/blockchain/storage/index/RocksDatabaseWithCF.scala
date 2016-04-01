package io.scalechain.blockchain.storage.index

import java.io.File
import java.util.{ArrayList, List}
import io.scalechain.blockchain.storage.{CFKeyValueDatabase, Storage}
import org.rocksdb._

/**
  * Created by mijeong on 3/25/16.
  */
class RocksDatabaseWithCF(path : File, columnFamilyNames : ArrayList[String]) extends KeyValueDatabase with CFKeyValueDatabase {
  assert( Storage.initialized )

  private val columnFamilyDescriptors = new ArrayList[ColumnFamilyDescriptor]
  columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()))
  for(i <- 0 until columnFamilyNames.size()) {
    columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamilyNames.get(i).getBytes(), new ColumnFamilyOptions()))
  }

  private val columnFamilyHandles = new ArrayList[ColumnFamilyHandle]()

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  private val options = new Options().setCreateIfMissing(true)
  private val db = RocksDB.open(new DBOptions().setCreateIfMissing(true), path.getAbsolutePath, columnFamilyDescriptors, columnFamilyHandles)

  def createColumnFamily(descriptor: String) = {

    // create new column family descriptor and new column family handler
    val columnFamilyHandle = db.createColumnFamily(
      new ColumnFamilyDescriptor(descriptor.getBytes(),
        new ColumnFamilyOptions())
    )

    columnFamilyHandles.add(columnFamilyHandle)
  }

  def listColumnFamilies(): List[Array[Byte]] = {

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
    if(index > 0)
      columnFamilyHandles.get(index)
    else
      null
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

  def getKeys(cf: ColumnFamilyHandle): ArrayList[String] = {
    val keys = new ArrayList[String]
    val cfIterator = iterator(cf)
    cfIterator.seekToFirst()

    while(cfIterator.isValid) {
      keys.add(new String(cfIterator.key()))
      cfIterator.next()
    }

    keys
  }

  def getKeysByteArray(cf: ColumnFamilyHandle): ArrayList[Array[Byte]] = {
    val keys = new ArrayList[Array[Byte]]
    val cfIterator = iterator(cf)
    cfIterator.seekToFirst()

    while(cfIterator.isValid) {
      keys.add(cfIterator.key())
      cfIterator.next()
    }

    keys
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

  def iterator(cf: ColumnFamilyHandle): RocksIterator = {
    db.newIterator(cf)
  }

  def close() = {
    db.close
  }
}
