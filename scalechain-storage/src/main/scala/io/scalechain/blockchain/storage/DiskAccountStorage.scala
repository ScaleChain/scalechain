package io.scalechain.blockchain.storage

import java.io.File
import java.util.ArrayList

import io.scalechain.util._
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{WalletTransactionDetailCodec, AccountCodec, AddressCodec}
import io.scalechain.blockchain.proto.walletparts.{WalletTransactionDetail, AccountHeader, Account, Address}
import io.scalechain.blockchain.storage.index.{TransactionDatabase, RocksDatabaseWithCF, AccountDatabase}
import io.scalechain.blockchain.storage.record.{TransactionRecordStorage, AccountRecordStorage}
import io.scalechain.util.ByteArray
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by mijeong on 2016. 3. 24..
  */
object DiskAccountStorage {

  var columnFamilyName = new ArrayList[String]
  var transactionColumnFamilyName = new ArrayList[String]
  val addressAccountCF = "addressAccount"

  val prefix = "wallet-"
  val countFromBack = 9

  val transactionCFFileName = "/transaction-cf"

  var accountDirectoryPath : File = null
  var transactionDirectoryPath : File = null

  protected[storage] var accountIndex : AccountDatabase = _
  protected[storage] var accountRecordStorage : AccountRecordStorage = _

  protected[storage] var transactionIndex : TransactionDatabase = _
  protected[storage] var transactionStorage : TransactionRecordStorage = _

  /**
    * open account index (RocksDB with Column Families)
    *
    */
  def open(accountDirectory : String, transactionDirectory : String) = {

    accountDirectoryPath = new File(accountDirectory)
    transactionDirectoryPath = new File(transactionDirectory)
    accountDirectoryPath.mkdir()
    transactionDirectoryPath.mkdir()

    // 1. open account database with column families
    val dir = new File(accountDirectoryPath.getAbsolutePath)
    val files : java.util.List[File] =
      FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).asInstanceOf[java.util.List[File]]
    var accountExist = false

    // 1.1 getting column families for account
    for(i <- 0 until files.size()) {
      val file = files.get(i)
      val name = file.getName

      if(name.contains(DiskAccountStorage.prefix)) {
        val columnFamily = name.substring(DiskAccountStorage.prefix.length, name.length-DiskAccountStorage.countFromBack)
        DiskAccountStorage.columnFamilyName.add(AccountDatabase.ACCOUNT_INFO + columnFamily)
        accountExist = true
      }
    }

    if(accountExist)
      DiskAccountStorage.columnFamilyName.add(DiskAccountStorage.addressAccountCF)

    // 2. open wallet transaction database with column families
    val transactionDir = new File(transactionDirectoryPath.getAbsolutePath)
    val transactionColumnFamilyFileExist = new File(transactionDir + DiskAccountStorage.transactionCFFileName).exists()

    // 2.1 getting column families for transaction
    if(transactionColumnFamilyFileExist) {
      for (columnFamily <- Source.fromFile(transactionDir + DiskAccountStorage.transactionCFFileName).getLines()) {
        DiskAccountStorage.transactionColumnFamilyName.add(columnFamily)
      }
    } else {
      new File(transactionDir + DiskAccountStorage.transactionCFFileName).createNewFile()
    }

    accountIndex = new AccountDatabase(new RocksDatabaseWithCF(accountDirectoryPath, DiskAccountStorage.columnFamilyName))

    transactionIndex = new TransactionDatabase(
      new RocksDatabaseWithCF(transactionDirectoryPath, DiskAccountStorage.transactionColumnFamilyName))
    transactionStorage = new TransactionRecordStorage(transactionDirectoryPath)
  }

  def create(accountDirectory : String, transactionDirectory : String) : (AccountDatabase, AccountRecordStorage, TransactionDatabase, TransactionRecordStorage) = {

    if(accountIndex==null)
      open(accountDirectory, transactionDirectory)

    (accountIndex, accountRecordStorage, transactionIndex, transactionStorage)
  }
}

class DiskAccountStorage(accountDirectoryPath : File, transactionDirectoryPath : File) {
  private val logger = LoggerFactory.getLogger(classOf[DiskAccountStorage])

  protected[storage] var accountIndex : AccountDatabase = _
  protected[storage] var accountRecordStorage : AccountRecordStorage = _

  protected[storage] var transactionIndex : TransactionDatabase = _
  protected[storage] var transactionStorage : TransactionRecordStorage = _

  val (singleAccountIndex, singleAccountRecordStorage, singleTransactionIndex, singleRransactionStorage)
  = DiskAccountStorage.create(accountDirectoryPath.getAbsolutePath, transactionDirectoryPath.getAbsolutePath)
  accountIndex = singleAccountIndex
  accountRecordStorage = singleAccountRecordStorage
  transactionIndex = singleTransactionIndex
  transactionStorage = singleRransactionStorage

  /**
    * put new address and account info to record and index
    *
    * used by getnewaddress RPC
    */
  def putNewAddress(account : String, address : String, purpose : Int, publicKey : ByteArray, privateKey : ByteArray) : Option[String] = {

    accountRecordStorage = new AccountRecordStorage(accountDirectoryPath, account)

    // 1. if account already exists
    if(accountIndex.existAccount(account)) {
      // 1.1 create new address info
      val newAddress = Address (
        address = address,
        publicKey = publicKey,
        privateKey = privateKey,
        purpose = purpose
      )

      // 1.2 append new address info to record
      val locator = accountRecordStorage.appendRecord(newAddress)(AddressCodec)
      val privateKeyLocator = getPrivateKeyLocator(locator)

      val newAddressInfo = AddressInfo(
        account,
        purpose,
        publicKey,
        Some(privateKeyLocator)
      )

      // 1.3 put new address info to index
      accountIndex.putAddressInfo(account, AddressKey(address), newAddressInfo)

    // 2. if account does not exist
    } else {
      // 2.1 create new account info
      val newAddress = Address (
        address = address,
        publicKey = publicKey,
        privateKey = privateKey,
        purpose = purpose
      )

      val newHeader = AccountHeader(
        version = 1,
        timestamp = System.currentTimeMillis() / 1000
      )

      val newAccount = Account (
        header = newHeader,
        account = account,
        addresses = newAddress :: Nil
      )

      // 2.2 create new record and append new account info
      val locator = accountRecordStorage.appendRecord(newAccount)(AccountCodec)
      val privateKeyLocator = getPrivateKeyLocator(locator)

      val newAddressInfo = AddressInfo(
        account,
        purpose,
        publicKey,
        Some(privateKeyLocator)
      )

      // 2.3 put new address info to index
      accountIndex.putAddressInfo(account, AddressKey(address), newAddressInfo)
      DiskAccountStorage.columnFamilyName.add(account)
    }

    accountIndex.putAccount(DiskAccountStorage.addressAccountCF, AddressKey(address), AccountInfo(account))
    accountRecordStorage.close()
    Some(address)
  }

  /**
    * get private key record locator
    *
    * @param fileRecordLocator record locator of address info
    * @return private key record locator
    */
  def getPrivateKeyLocator(fileRecordLocator : FileRecordLocator) : FileRecordLocator = {
    val originRecordLocator = fileRecordLocator.recordLocator

    val privateKeyLocator = FileRecordLocator(
      fileIndex = fileRecordLocator.fileIndex,
      recordLocator = RecordLocator (
        offset = originRecordLocator.offset,
        size = 33
      )
    )

    privateKeyLocator
  }

  /**
    * get account name associated with address
    *
    * @param address
    * @return account name
    */
  def getAccount(address : String) : Option[String] = {
    val accountOption = accountIndex.getAccount(DiskAccountStorage.addressAccountCF, AddressKey(address))
    accountOption
  }

  /**
    * get address not yet received
    *
    * @param account
    * @param address
    * @param purpose
    * @param publicKey
    * @param privateKey
    * @return address not yet received
    */
  def getReceiveAddress(account : String, address : String, purpose : Int, publicKey : ByteArray, privateKey : ByteArray) : String = {

    // 1. if there is account in database
    if(accountIndex.existAccount(account)) {

      val addressOption = accountIndex.getReceiveAddress(account)
      // 1.1 if there is address not yet received
      if(addressOption.isDefined) {
        addressOption.get

      // 1.2 if there is no address not yet received
      } else {
        val newAddressOption = putNewAddress(account, address, purpose, publicKey, privateKey)
        newAddressOption.get
      }
    // 2. if there is no account in database
    } else {
      val newAddressOption = putNewAddress(account, address, purpose, publicKey, privateKey)
      newAddressOption.get
    }
  }

  /**
    * put wallet transaction to index
    *
    * @param account
    * @param txid
    * @param walletTransactionDetail
    * @param category
    */
  def putTransaction(account : String, txid : Hash, walletTransactionDetail : WalletTransactionDetail, category : Int) = {
    val locator = transactionStorage.appendRecord(walletTransactionDetail)(WalletTransactionDetailCodec)

    val walletTransactionInfo = WalletTransactionInfo(
      category,
      Some(locator)
    )
    transactionIndex.putTransaction(account, txid, walletTransactionInfo)
  }

  /**
    * get wallet transaction list
    *
    * @param account
    * @param count
    * @param skip
    * @param includeWatchOnly
    * @return wallet transaction list
    */
  def getTransactionList(account : String, count : Int, skip : Int, includeWatchOnly : Boolean) : Option[List[WalletTransactionDetail]] = {

    // 1. get all transactions associated with all accounts of wallet
    if(account.size == 0) {
      var transactionList = new ListBuffer[WalletTransactionDetail]()
      var transactionListResult = new ListBuffer[WalletTransactionDetail]()

      val transactionDir = new File(transactionDirectoryPath.getAbsolutePath)
      val transactionColumnFamilyFileExist = new File(transactionDir + DiskAccountStorage.transactionCFFileName).exists()

      if(transactionColumnFamilyFileExist) {
        // 1.1 loop for getting transactions of all accounts
        for (columnFamily <- Source.fromFile(transactionDir + DiskAccountStorage.transactionCFFileName).getLines()) {
          val accountTransactionList =
            getAccountTransactionList(columnFamily.substring(3, columnFamily.size), 0, 0, includeWatchOnly, true)
          if(accountTransactionList.isDefined)
            transactionList ++= accountTransactionList.get.to[ListBuffer]
        }

        for(i<- skip until (skip+count) if i < transactionList.size ) {
          transactionListResult += transactionList(i)
        }
      }

      if(transactionListResult.toList.length > 0) {
        Some(transactionList.toList)
      } else {
        None
      }

    // 2. get transactions associated with the given account
    } else {
      getAccountTransactionList(account, count, skip, includeWatchOnly, false)
    }
  }

  /**
    * get transaction list associated with the given account
    *
    * @param account
    * @param count
    * @param skip
    * @param includeWatchOnly
    * @return get transaction list
    */
  def getAccountTransactionList(account : String, count : Int, skip : Int, includeWatchOnly : Boolean, allStatus : Boolean) : Option[List[WalletTransactionDetail]] = {
    val transactionList = new ListBuffer[WalletTransactionDetail]()
    val txidListOption = transactionIndex.getTxidList(account)

    if(txidListOption.isDefined){
      val txidList = txidListOption.get
      var addCount = 0

      if(txidList.size() > skip) {
        // allStatus == true means getting all transactions regardless of count
        // used by listtransaction RPC (there is no given account)
        for(j <- 0 until txidList.size() if (allStatus || addCount < count)) {
          if(j >= skip) {
            val txid = Hash(Utils.reverseBytes(txidList.get(j)))
            val transaction = transactionIndex.getTransactionLocator(account, txid)
            val transactionRecord = transactionStorage.readRecord(transaction.get)(WalletTransactionDetailCodec)
            transactionList += transactionRecord
            addCount+=1
          }
        }
      }
    }

    if(transactionList.toList.length > 0) {
      Some(transactionList.toList)
    } else {
      None
    }
  }

  def close() = {
    this.synchronized {
      accountIndex.close()
      transactionIndex.close()
    }
  }

}
