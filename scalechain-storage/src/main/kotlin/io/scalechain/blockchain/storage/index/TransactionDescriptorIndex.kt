package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.TransactionDescriptor
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.TransactionDescriptorCodec
import io.scalechain.blockchain.proto.codec.HashCodec

interface TransactionDescriptorIndex {
//  private val logger = LoggerFactory.getLogger(TransactionDescriptorIndex::class.java)

  /**
    * Get the descriptor of a transaction by hash
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash.
    * @return Some(descriptor) if the transaction exists; None otherwise.
    */
  fun getTransactionDescriptor(db : KeyValueDatabase, txHash : Hash) : TransactionDescriptor? {
    //logger.trace(s"getTransactionDescriptor : ${txHash}")
    return db.getObject(HashCodec, TransactionDescriptorCodec, DB.TRANSACTION, txHash)
  }

  /**
    * Put the descriptor of a transaction with hash of it
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash.
    * @param transactionDescriptor The descriptor of the transaction.
    */
  fun putTransactionDescriptor(db : KeyValueDatabase, txHash : Hash, transactionDescriptor : TransactionDescriptor) : Unit {
    //logger.trace(s"putTransactionDescriptor : ${txHash}")
    db.putObject(HashCodec, TransactionDescriptorCodec, DB.TRANSACTION, txHash, transactionDescriptor)
  }

  /**
    * Del the descriptor of a transaction by hash.
    *
    * TODO : Add a test case
    *
    * @param txHash The transaction hash
    */
  fun delTransactionDescriptor(db : KeyValueDatabase, txHash : Hash) : Unit {
    //logger.trace(s"delTransactionDescriptor : ${txHash}")
    db.delObject(HashCodec, DB.TRANSACTION, txHash)
  }
}