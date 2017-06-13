package io.scalechain.blockchain.storage

import org.rocksdb.RocksDB

/**
  * Created by kangmo on 3/11/16.
  */
object Storage {
  var isInitialized : Boolean = false

  @JvmStatic
  fun initialized() = isInitialized

  @JvmStatic
  fun initialize() : Unit {
    RocksDB.loadLibrary()
    isInitialized = true
  }
}
