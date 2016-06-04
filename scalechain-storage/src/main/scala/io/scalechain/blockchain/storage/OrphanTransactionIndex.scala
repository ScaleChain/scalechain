package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.proto.codec.{HashCodec, OneByteCodec, OrphanTransactionDescriptorCodec}
import io.scalechain.blockchain.proto.{Hash, OneByte, OrphanTransactionDescriptor}
import io.scalechain.blockchain.storage.index.{DatabaseTablePrefixes, SharedKeyValueDatabase}
import io.scalechain.util.HexUtil._
import io.scalechain.util.Using._

/**
  * Provides index operations for orphan transactions.
  */
trait OrphanTransactionIndex extends SharedKeyValueDatabase {
  import DatabaseTablePrefixes._
  private implicit val hashCodec = HashCodec
  private implicit val orphanTransactionDescriptorCodec = OrphanTransactionDescriptorCodec
  private implicit val oneByteCodec = OneByteCodec

  /** Put an orphan transaction
    *
    * @param hash The hash of the transaction header.
    * @param orphanTransactionDescriptor The descriptor of the orphan transaction.
    */
  def putOrphanTransaction(hash : Hash, orphanTransactionDescriptor : OrphanTransactionDescriptor) : Unit = {
    keyValueDB.putObject(ORPHAN_TRANSACTION, hash, orphanTransactionDescriptor)
  }

  /** Get an orphan transaction by the hash of it.
    *
    * @param hash The orphan transaction header.
    * @return Some(transaction) if an orphan transaction was found by the hash. None otherwise.
    */
  def getOrphanTransaction(hash : Hash) : Option[OrphanTransactionDescriptor] = {
    keyValueDB.getObject(ORPHAN_TRANSACTION, hash)(HashCodec, OrphanTransactionDescriptorCodec)
  }

  /** Delete a specific orphan transaction.
    *
    * @param hash The hash of the orphan transaction.
    */
  def delOrphanTransaction(hash : Hash) : Unit = {
    keyValueDB.delObject(ORPHAN_TRANSACTION, hash)
  }

  /** Add an orphan transaction than depends on a transaction denoted by the hash.
    *
    * @param missingTransactionHash The hash of the missing transaction that the orphan transaction depends on.
    * @param orphanTransactionHash The hash of the orphan transaction.
    */
  def addOrphanTransactionByParent(missingTransactionHash : Hash, orphanTransactionHash : Hash) : Unit = {
    // TODO : Optimize : Reduce the length of the prefix string by using base64 encoding?
    keyValueDB.putPrefixedObject(ORPHAN_TRANSACTIONS_BY_DEPENDENCY, hex(missingTransactionHash.value), orphanTransactionHash, OneByte(1) )
  }

  /** Get all orphan transactions that depend on the given transaction.
    *
    * @param missingTransactionHash The hash of the missing transaction that the orphan transaction depends on.
    * @return Hash of all orphan transactions that depend on the given missing transaction.
    */
  def getOrphanTransactionsByParent(missingTransactionHash : Hash) : List[Hash] = {
    (
      using(keyValueDB.seekPrefixedObject(ORPHAN_TRANSACTIONS_BY_DEPENDENCY, hex(missingTransactionHash.value))(HashCodec, OneByteCodec)) in {
        _.toList
      }
    ).map{ case (CStringPrefixed(_, hash : Hash), _) => hash }
  }

  /** Del all orphan transactions that depend on the given missing transaction.
    *
    * @param missingTransactionHash The hash of the missing transaction that the orphan transactions depend on.
    */
  def delOrphanTransactionsByParent(missingTransactionHash : Hash) : Unit = {
    getOrphanTransactionsByParent(missingTransactionHash) foreach { transactionHash : Hash =>
      keyValueDB.delPrefixedObject(ORPHAN_TRANSACTIONS_BY_DEPENDENCY, hex(missingTransactionHash.value), transactionHash)
    }
  }
}
