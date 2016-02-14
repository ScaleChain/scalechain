package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Hash, Transaction}

/**
  * Created by kangmo on 2/14/16.
  */
trait TransactionStorage {
  def storeTransaction(transaction : Transaction)

  def getTransaction(hash : Hash ) : Option[Transaction]

  def hasTransaction(hash : Hash): Boolean
}
