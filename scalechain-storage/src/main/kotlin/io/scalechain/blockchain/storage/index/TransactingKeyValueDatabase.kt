package io.scalechain.blockchain.storage.index

/**
 * Created by kangmo on 17/12/2016.
 */
interface TransactingKeyValueDatabase : KeyValueDatabase {
  fun beginTransaction() : Unit
  fun abortTransaction() : Unit
  fun commitTransaction() : Unit

  override fun transacting() : TransactingKeyValueDatabase {
    // transacting should never called for a TransactingKeyValueDatabase. It can be called by non-transactional database instances to support transaction feature.
    throw AssertionError()
  }

}