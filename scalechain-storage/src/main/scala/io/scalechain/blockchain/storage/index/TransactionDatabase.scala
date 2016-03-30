package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.codec.{FileRecordLocatorCodec, WalletTransactionInfoCodec, HashCodec}
import io.scalechain.blockchain.proto.{WalletTransactionInfo, FileRecordLocator, Hash}
import io.scalechain.blockchain.storage.CFKeyValueDatabase

/**
  * Created by mijeong on 2016. 3. 30..
  */
object TransactionDatabase {
  val WALLET_TRANSACTION_INFO : Byte = 't'
  val WALLET_UNSPENT_TRANSACTION_INFO : Byte = 'u'
}

class TransactionDatabase(db : CFKeyValueDatabase) {

  def putTransaction(address : String, txid : Hash, walletTransactionInfo: WalletTransactionInfo) = {

    db.putObject(TransactionDatabase.WALLET_TRANSACTION_INFO + address, txid, walletTransactionInfo)(HashCodec, WalletTransactionInfoCodec)
  }

  def getTransactionLocator(address : String, txid : Hash) : Option[FileRecordLocator] = {

    val walletTransactionInfo = db.getObject(TransactionDatabase.WALLET_TRANSACTION_INFO + address, txid)(HashCodec, WalletTransactionInfoCodec)
    if(walletTransactionInfo.isDefined)
      walletTransactionInfo.get.txLocator
    else
      None
  }

  def putUnspentTransaction(address : String, txid : Hash, txLocator : FileRecordLocator) = {
    db.putObject(TransactionDatabase.WALLET_UNSPENT_TRANSACTION_INFO + address, txid, txLocator)(HashCodec, FileRecordLocatorCodec)
  }

  def getUnspentTransactionLocator(address : String, txid : Hash) : Option[FileRecordLocator] = {
    db.getObject(TransactionDatabase.WALLET_UNSPENT_TRANSACTION_INFO + address, txid)(HashCodec, FileRecordLocatorCodec)
  }

  def close() = db.close()

}
