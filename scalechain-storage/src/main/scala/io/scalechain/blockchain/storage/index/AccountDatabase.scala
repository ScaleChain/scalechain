package io.scalechain.blockchain.storage.index

import java.nio.ByteBuffer

import io.scalechain.blockchain.proto.{FileRecordLocator, AccountInfo, AddressKey, AddressInfo}
import io.scalechain.blockchain.proto.codec.{AccountInfoCodec, AddressKeyCodec, AddressInfoCodec}
import io.scalechain.blockchain.storage.CFKeyValueDatabase

/**
  * Created by mijeong on 2016. 3. 22..
  */

// TODO: define all values for account database
object AccountDatabase {
  val ACCOUNT_INFO : Byte = 'a'
  val RECEIVED_ADDRESS = ByteBuffer.wrap(Array[Byte](1, 0, 0, 0)).getInt
}

class AccountDatabase(db : CFKeyValueDatabase) {

  /**
    * put new address info or change address info
    *
    * @param columnFamilyName account name
    * @param address key
    * @param addressInfo value
    */
  def putAddressInfo(columnFamilyName : String, address : AddressKey, addressInfo : AddressInfo) = {
    val addressInfoOption = getAddressInfo(columnFamilyName, address)

    if(addressInfoOption.isDefined) {
      val currentAddressInfo = addressInfoOption.get

      // account name can not be changed
      assert(currentAddressInfo.account == addressInfo.account)

      // public key can not be changed
      assert(currentAddressInfo.publicKey == addressInfo.publicKey)
    }

    db.putObject(columnFamilyName, address, addressInfo)(AddressKeyCodec, AddressInfoCodec)
  }

  /**
    * put new account info
    *
    * @param columnFamilyName "addressaccount" fixed
    * @param address key
    * @param accountInfo value
    */
  def putAccount(columnFamilyName : String, address : AddressKey, accountInfo : AccountInfo) = {
    val accountInfoOption = getAccount(columnFamilyName, address)

    // account name can not be changed
    if(accountInfoOption.isDefined) {
      assert(false)
    }

    db.putObject(columnFamilyName, address, accountInfo)(AddressKeyCodec, AccountInfoCodec)
  }

  /**
    * get address info using column family and key
    *
    * @param columnFamilyName account name
    * @param address key
    * @return address info or nothing (if account or address does not exist
    */
  def getAddressInfo(columnFamilyName : String, address : AddressKey) : Option[AddressInfo] = {
    db.getObject(columnFamilyName, address)(AddressKeyCodec, AddressInfoCodec)
  }

  /**
    * get account name using column family and key
    *
    * @param columnFamilyName "addressaccount" fixed
    * @param address key
    * @return account name or nothing (if address does not exist)
    */
  def getAccount(columnFamilyName : String, address : AddressKey) : Option[String] = {

    val addressInfoOption = db.getObject(columnFamilyName, address)(AddressKeyCodec, AccountInfoCodec)
    if(addressInfoOption.isDefined) {
      val addressInfo = addressInfoOption.get
      Some(addressInfo.account)
    } else {
      None
    }
  }

  /**
    * get received address of a specific account
    *
    * @param columnFamilyName account name
    * @return address info including received address
    */
  def getReceivedAddress(columnFamilyName : String) : Option[AddressInfo] = {

    // 1. get all keys associated with column family name(account)
    val keys = db.getKeys(columnFamilyName)

    var findReceived = false
    var result : Option[AddressInfo] = None

    // 2. find address info including received address
    for(i <- 0 until keys.size(); if findReceived != true) {

      // TODO: Find reason, why double quotation is attached in front
      val key = keys.get(i)
      val addressInfo = getAddressInfo(columnFamilyName, AddressKey(key.substring(1, key.size)))
      // 2.1 check purpose of address info
      if(addressInfo.get.purpose == AccountDatabase.RECEIVED_ADDRESS) {
        findReceived = true
        result = addressInfo
      }
    }

    result
  }

  /**
    * get private key locator in record
    *
    * @param columnFamilyName account name
    * @param address key
    * @return private key locator in record
    */
  def getPrivateKeyLocator(columnFamilyName : String, address : AddressKey) : Option[FileRecordLocator] = {

    val addressInfo = getAddressInfo(columnFamilyName, address)
    if(addressInfo.isDefined) {
      addressInfo.get.privateKeyLocator
    } else {
      None
    }
  }

  /**
    * check if columnFamilyName(account) exists
    */
  def existAccount(columnFamilyName : String) : Boolean = {
    val index = db.getColumnFamilyIndex(columnFamilyName)

    if(index == -1) {
      false
    } else {
      true
    }
  }

  def close() = db.close()

}
