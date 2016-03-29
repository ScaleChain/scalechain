package io.scalechain.blockchain.storage

import java.io.File
import java.util._

import io.scalechain.blockchain.proto.{RecordLocator, FileRecordLocator, AddressInfo, AddressKey}
import io.scalechain.blockchain.proto.codec.{AccountCodec, AddressCodec}
import io.scalechain.blockchain.proto.walletparts.{AccountHeader, Account, Address}
import io.scalechain.blockchain.storage.index.{RocksDatabaseWithCF, AccountDatabase}
import io.scalechain.blockchain.storage.record.AccountRecordStorage
import io.scalechain.util.ByteArray
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.slf4j.LoggerFactory

/**
  * Created by mijeong on 2016. 3. 24..
  */
object DiskAccountStorage {

  var columnFamilyName = new ArrayList[String]
  val path = "./target/accountdata/"
  val prefix = "wallet-"
  val countFromBack = 9
}

class DiskAccountStorage(directoryPath : File) {
  private val logger = LoggerFactory.getLogger(classOf[DiskAccountStorage])

  directoryPath.mkdir()

  protected[storage] var accountIndex : AccountDatabase = _
  protected[storage] var accountRecordStorage : AccountRecordStorage = _

  /**
    * open account index (RocksDB with Column Families)
    *
    */
  def open = {
    val dir = new File(DiskAccountStorage.path)
    val files : List[File] = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).asInstanceOf[List[File]]

    for(i <- 0 until files.size()) {
      val file = files.get(i)
      val name = file.getName

      if(name.contains(DiskAccountStorage.prefix)) {
        val columnFamily = name.substring(DiskAccountStorage.prefix.length, name.length-DiskAccountStorage.countFromBack)
        DiskAccountStorage.columnFamilyName.add(columnFamily)
      }
    }

    accountIndex = new AccountDatabase(new RocksDatabaseWithCF(directoryPath, DiskAccountStorage.columnFamilyName))
  }

  /**
    * put new address and account info to record and index
    *
    * used by getnewaddress RPC
    */
  def putNewAddress(account : String, address : String, purpose : Int, publicKey : ByteArray, privateKey : ByteArray) : Option[String] = {

    accountRecordStorage = new AccountRecordStorage(directoryPath, account)

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

  def close() = {
    this.synchronized {
      accountRecordStorage.close()
      accountIndex.close()
    }
  }
}
