package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.proto.codec.OneByteCodec
import io.scalechain.blockchain.proto.codec.OrphanTransactionDescriptorCodec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.OrphanTransactionDescriptor
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.DB
import io.scalechain.util.HexUtil

//import io.scalechain.util.Using._

/**
  * Provides index operations for orphan transactions.
  */
interface OrphanTransactionIndex {
/*
  private implicit val hashCodec = HashCodec
  private implicit val orphanTransactionDescriptorCodec = OrphanTransactionDescriptorCodec
  private implicit val oneByteCodec = OneByteCodec
*/
  /** Put an orphan transaction
    *
    * @param hash The hash of the transaction header.
    * @param orphanTransactionDescriptor The descriptor of the orphan transaction.
    */
  fun putOrphanTransaction(db : KeyValueDatabase, hash : Hash, orphanTransactionDescriptor : OrphanTransactionDescriptor) : Unit {
    db.putObject(HashCodec, OrphanTransactionDescriptorCodec, DB.ORPHAN_TRANSACTION, hash, orphanTransactionDescriptor)
  }

  /** Get an orphan transaction by the hash of it.
    *
    * @param hash The orphan transaction header.
    * @return Some(transaction) if an orphan transaction was found by the hash. None otherwise.
    */
  fun getOrphanTransaction(db : KeyValueDatabase, hash : Hash) : OrphanTransactionDescriptor? {
    return db.getObject(HashCodec, OrphanTransactionDescriptorCodec, DB.ORPHAN_TRANSACTION, hash)
  }

  /** Delete a specific orphan transaction.
    *
    * @param hash The hash of the orphan transaction.
    */
  fun delOrphanTransaction(db : KeyValueDatabase, hash : Hash) : Unit {
    db.delObject(HashCodec, DB.ORPHAN_TRANSACTION, hash)
  }

  /** Add an orphan transaction than depends on a transaction denoted by the hash.
    *
    * @param missingTransactionHash The hash of the missing transaction that the orphan transaction depends on.
    * @param orphanTransactionHash The hash of the orphan transaction.
    */
  fun addOrphanTransactionByParent(db : KeyValueDatabase, missingTransactionHash : Hash, orphanTransactionHash : Hash) : Unit {
    // TODO : Optimize : Reduce the length of the prefix string by using base64 encoding?
    db.putPrefixedObject(HashCodec, OneByteCodec, DB.ORPHAN_TRANSACTIONS_BY_DEPENDENCY, HexUtil.hex(missingTransactionHash.value), orphanTransactionHash, OneByte(1) )
  }

  /** Get all orphan transactions that depend on the given transaction.
    *
    * @param missingTransactionHash The hash of the missing transaction that the orphan transaction depends on.
    * @return Hash of all orphan transactions that depend on the given missing transaction.
    */
  fun getOrphanTransactionsByParent(db : KeyValueDatabase, missingTransactionHash : Hash) : List<Hash> {
    val iterator = db.seekPrefixedObject(HashCodec, OneByteCodec, DB.ORPHAN_TRANSACTIONS_BY_DEPENDENCY, HexUtil.hex(missingTransactionHash.value))
    try {

      // BUGBUG : Change the code not to use Pair, but a data class. This is code so hard to read.
      return iterator.asSequence().toList().map { prefixedObject ->
        val cstringPrefixed = prefixedObject.first
        val hash = cstringPrefixed.data
        hash
      }
    } finally {
      iterator.close()
    }
  }

  /** Del all orphan transactions that depend on the given missing transaction.
    *
    * @param missingTransactionHash The hash of the missing transaction that the orphan transactions depend on.
    */
  fun delOrphanTransactionsByParent(db : KeyValueDatabase, missingTransactionHash : Hash) : Unit {
    for (transactionHash : Hash in getOrphanTransactionsByParent(db, missingTransactionHash)) {
      db.delPrefixedObject(HashCodec, DB.ORPHAN_TRANSACTIONS_BY_DEPENDENCY, HexUtil.hex(missingTransactionHash.value), transactionHash)
    }
  }
}
