package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.{AddressInfo, AddressKey}
import io.scalechain.blockchain.proto.codec.{AccountCodec, AddressCodec}
import io.scalechain.blockchain.proto.walletparts.{AccountHeader, Account, Address}
import io.scalechain.blockchain.storage.index.{RocksDatabase, AccountDatabase}
import io.scalechain.blockchain.storage.record.AccountRecordStorage
import io.scalechain.util.ByteArray
import org.slf4j.LoggerFactory

/**
  * Created by mijeong on 2016. 3. 24..
  */
object DiskAccountStorage {
  var theAccountStorage : DiskAccountStorage = null

  def create(storagePath : File, account : String) : DiskAccountStorage = {
    assert(theAccountStorage == null)
    theAccountStorage = new DiskAccountStorage(storagePath, account)
    theAccountStorage
  }

  /** Get the account storage. This actor is a singleton.
    *
    * @return The account storage.
    */
  def get() : DiskAccountStorage = {
    assert(theAccountStorage != null)
    theAccountStorage
  }
}

class DiskAccountStorage(directoryPath : File, account : String) {
  private val logger = LoggerFactory.getLogger(classOf[DiskAccountStorage])

  directoryPath.mkdir()

  protected[storage] val accountIndex = new AccountDatabase( new RocksDatabase( directoryPath ) )
  protected[storage] val accountRecordStorage = new AccountRecordStorage(directoryPath, account)

  /**
    * put new address and account info to record and index
    *
    * used by getnewaddress RPC
    */
  def putNewAddress(account : String, address : String, purpose : Int, publicKey : ByteArray, privateKey : ByteArray) : Option[String] = {

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

      val newAddressInfo = AddressInfo(
        account,
        purpose,
        publicKey,
        Some(locator)
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

      val newAddressInfo = AddressInfo(
        account,
        purpose,
        publicKey,
        Some(locator)
      )

      // 2.3 put new address info to index
      accountIndex.putAddressInfo(account, AddressKey(address), newAddressInfo)
    }

    Some(address)
  }

  def close() = {
    this.synchronized {
      accountRecordStorage.close()
      accountIndex.close()
    }
  }
}
