package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.{TransactionDescriptor, Hash}
import io.scalechain.blockchain.proto.codec.{TransactionDescriptorCodec, TransactionPoolEntryCodec, HashCodec}

trait TransactionDescriptorIndex extends SharedKeyValueDatabase {
  import DatabaseTablePrefixes._
  private implicit val hashCodec = HashCodec
  private implicit val transactionCodec = TransactionPoolEntryCodec

  /**
    * Get the descriptor of a transaction by hash
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash.
    * @return Some(descriptor) if the transaction exists; None otherwise.
    */
  def getTransactionDescriptor(txHash : Hash) : Option[TransactionDescriptor] = {
    keyValueDB.getObject(TRANSACTION, txHash)(HashCodec, TransactionDescriptorCodec)
  }

  /**
    * Put the descriptor of a transaction with hash of it
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash.
    * @param transactionDescriptor The descriptor of the transaction.
    */
  def putTransactionDescriptor(txHash : Hash, transactionDescriptor : TransactionDescriptor) = {
    keyValueDB.putObject(TRANSACTION, txHash, transactionDescriptor)(HashCodec, TransactionDescriptorCodec)
  }

  /**
    * Del the descriptor of a transaction by hash.
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash
    */
  def delTransactionDescriptor(txHash : Hash) : Unit = {
    keyValueDB.delObject(TRANSACTION, txHash)(HashCodec)
  }
}