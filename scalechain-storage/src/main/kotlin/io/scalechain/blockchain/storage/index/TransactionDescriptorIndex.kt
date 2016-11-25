package io.scalechain.blockchain.storage.index

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.{TransactionDescriptor, Hash}
import io.scalechain.blockchain.proto.codec.{TransactionDescriptorCodec, TransactionPoolEntryCodec, HashCodec}
import org.slf4j.LoggerFactory

trait TransactionDescriptorIndex {
  private val logger = LoggerFactory.getLogger(TransactionDescriptorIndex::class.java)

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
  fun getTransactionDescriptor(txHash : Hash)(implicit db : KeyValueDatabase) : Option<TransactionDescriptor> {
    //logger.trace(s"getTransactionDescriptor : ${txHash}")
    db.getObject(TRANSACTION, txHash)(HashCodec, TransactionDescriptorCodec)
  }

  /**
    * Put the descriptor of a transaction with hash of it
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash.
    * @param transactionDescriptor The descriptor of the transaction.
    */
  fun putTransactionDescriptor(txHash : Hash, transactionDescriptor : TransactionDescriptor)(implicit db : KeyValueDatabase) {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")
    db.putObject(TRANSACTION, txHash, transactionDescriptor)(HashCodec, TransactionDescriptorCodec)
  }

  /**
    * Del the descriptor of a transaction by hash.
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash
    */
  fun delTransactionDescriptor(txHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    //logger.trace(s"delTransactionDescriptor : ${txHash}")
    db.delObject(TRANSACTION, txHash)(HashCodec)
  }
}