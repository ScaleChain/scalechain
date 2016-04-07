package io.scalechain.blockchain.storage

import java.io.File
import java.util.ArrayList

import io.scalechain.blockchain.proto.codec.AccountCodec
import io.scalechain.blockchain.proto.walletparts.{Account, AccountHeader, Address}
import io.scalechain.blockchain.storage.index.AccountDatabase
import io.scalechain.blockchain.storage.record.AccountRecordStorage
import io.scalechain.util.HexUtil._
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 25..
  */
class DiskAccountStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var storage : DiskAccountStorage = null
  var accountTestPath : File = null
  var transactionTestPath : File = null

  transactionTestPath = new File("./target/unittests-DiskTransactionStorageSpec/")
  accountTestPath = new File("./target/unittests-DiskAccountStorageSpec/")

  FileUtils.deleteDirectory(transactionTestPath)
  FileUtils.deleteDirectory(accountTestPath)
  transactionTestPath.mkdir()
  accountTestPath.mkdir()

  DiskAccountStorage.columnFamilyName = new ArrayList[String]
  storage = new DiskAccountStorage(accountTestPath, transactionTestPath)

  "putNewAddress" should "pass case 1 : append new address info to record & put new address info to index" in {
    val account = "account"
    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val publicKey1 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey1 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose1 = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    val address2 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5bbbb"
    val publicKey2 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863d1234")
    val privateKey2 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa2331234")
    val purpose2 = AccountDatabase.ADDRESS_RECEIVED_PURPOSE

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address1, purpose1, publicKey1, privateKey1) shouldBe Some(address1)

    storage.accountIndex.existAccount(account) shouldBe true
    storage.putNewAddress(account, address2, purpose2, publicKey2, privateKey2) shouldBe Some(address2)
  }

  "putNewAddress" should "pass case 2 : create new record and append new account info & put new address info to index" in {
    val account = "account2"
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcuc"
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address)
  }

  "getPrivateKeyLocator" should "return file record locator for private key" in {

    val newAddress = Address (
      address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum",
      publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"),
      privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9"),
      purpose = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE
    )

    val newHeader = AccountHeader(
      version = 1,
      timestamp = System.currentTimeMillis() / 1000
    )

    val newAccount = Account (
      header = newHeader,
      account = "test",
      addresses = newAddress :: Nil
    )

    storage.accountRecordStorage = new AccountRecordStorage(accountTestPath, newAccount.account)
    val locator = storage.accountRecordStorage.appendRecord(newAccount)(AccountCodec)
    val privateKeyLocator = storage.getPrivateKeyLocator(locator)

    privateKeyLocator.recordLocator.size shouldBe 33
  }

  "getAccount" should "return account name associated with address" in {

    val account = "account1"
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcud"
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address)

    storage.getAccount(address) shouldBe Some(account)
  }

  "getAccount" should "return None if there is no account associated with address" in {

    val account1 = "account3"
    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcuf"
    val publicKey1 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey1 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose1 = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    val address2 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5bbb2"

    storage.accountIndex.existAccount(account1) shouldBe false
    storage.putNewAddress(account1, address1, purpose1, publicKey1, privateKey1) shouldBe Some(address1)

    storage.getAccount(address1) shouldBe Some(account1)
    storage.getAccount(address2) shouldBe None
  }

  "getReceiveAddress" should "return address for receiving payments" in {

    val account = "account5"
    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcub"
    val publicKey1 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey1 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose1 = AccountDatabase.ADDRESS_RECEIVED_PURPOSE

    val address2 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5bbbs"
    val publicKey2 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863d1234")
    val privateKey2 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa2331234")
    val purpose2 = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address1, purpose1, publicKey1, privateKey1) shouldBe Some(address1)

    storage.accountIndex.existAccount(account) shouldBe true
    storage.putNewAddress(account, address2, purpose2, publicKey2, privateKey2) shouldBe Some(address2)

    storage.getReceiveAddress(account, address1, purpose1, publicKey1, privateKey1) shouldBe address2
  }

  "getReceiveAddress" should "return new address if there is no address not yet received" in {

    val account = "account6"
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcu8"
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_RECEIVED_PURPOSE

    storage.accountIndex.existAccount(account) shouldBe false

    storage.getReceiveAddress(account, address, purpose, publicKey, privateKey) shouldBe a [String]
  }

  "getReceiveAddress" should "return new address if there is no given account in database" in {

    val account = "account"
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_RECEIVED_PURPOSE

    storage.getReceiveAddress(account, address, purpose, publicKey, privateKey) shouldBe a [String]

    storage.accountIndex.existAccount(account) shouldBe true
    storage.getAccount(address) shouldBe Some(account)
  }

}
