package io.scalechain.blockchain.storage.index

/**
  * A trait that shares a key-value database. All traits that wants to share the key-value database should extend from this trait.
  */
trait SharedKeyValueDatabase {
  /** The key-value database that traits extending this trait share.
    */
  protected[storage] val keyValueDB : KeyValueDatabase
}
