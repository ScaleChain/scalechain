package io.scalechain.blockchain.storage

import org.rocksdb.RocksDB

/**
  * Created by kangmo on 3/11/16.
  */
object Storage {
  var isInitialized : Boolean = false
  def initialized() = isInitialized
  def initialize() : Unit = {
    RocksDB.loadLibrary()
    isInitialized = true
  }
}
