package io.scalechain.blockchain.storage.index

/**
 * Created by kangmo on 18/12/2016.
 */
class TransactingMapDatabase(private val db : MapDatabase) : TransactingKeyValueDatabase {
  override fun beginTransaction() {
    // nothing to do. the gloal transaction in db.getDb() already started.

  }

  override fun commitTransaction() {
    db.getDb().commit()
  }

  override fun abortTransaction() {
    db.getDb().rollback()
  }

  override fun put(key: ByteArray, value: ByteArray) {
    db.getTreeMap().put(key, value)
  }

  override fun get(key: ByteArray): ByteArray? {
    return db.getTreeMap().get(key)
  }

  override fun del(key: ByteArray) {
    db.getTreeMap().remove(key)
  }

  override fun seek(keyOption: ByteArray?): ClosableIterator<Pair<ByteArray, ByteArray>> {
    return db.seek(keyOption)
  }

  @Deprecated("TransactingMapDatabase.transacting should never be called. transacting method can be called from a non-transactional MapDatabase only.", ReplaceWith(""), DeprecationLevel.ERROR)
  override fun transacting(): TransactingKeyValueDatabase {
    throw AssertionError()
  }

  @Deprecated("TransactingMapDatabase.close should never be called. close method can bel called from a non-transactional MapDatabase only.", ReplaceWith(""), DeprecationLevel.ERROR)
  override fun close() : Unit {
    throw AssertionError()
  }
}