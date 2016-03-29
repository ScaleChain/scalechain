package io.scalechain.blockchain.storage

import java.io.File
import java.util.ArrayList

import io.scalechain.blockchain.proto.codec.AccountCodec
import io.scalechain.blockchain.proto.walletparts.{Account, AccountHeader, Address}
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
  var testPath : File = null

  override def beforeEach() {

    testPath = new File("./target/unittests-DiskAccountStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    DiskAccountStorage.columnFamilyName = new ArrayList[String]
    storage = new DiskAccountStorage(testPath)
    storage.open

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
  }

  "putNewAddress" should "pass case 1 : append new address info to record & put new address info to index" in {
    val account = "account"
    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val publicKey1 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey1 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose1 = 1

    val address2 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5bbbb"
    val publicKey2 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863d1234")
    val privateKey2 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa2331234")
    val purpose2 = 2

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address1, purpose1, publicKey1, privateKey1) shouldBe Some(address1)

    storage.accountIndex.existAccount(account) shouldBe true
    storage.putNewAddress(account, address2, purpose2, publicKey2, privateKey2) shouldBe Some(address2)
  }

  "putNewAddress" should "pass case 2 : create new record and append new account info & put new address info to index" in {
    val account = "account"
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = 1

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address)
  }

  "getPrivateKeyLocator" should "return file record locator for private key" in {

    val newAddress = Address (
      address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum",
      publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"),
      privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9"),
      purpose = 1
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

    storage.accountRecordStorage = new AccountRecordStorage(testPath, newAccount.account)
    val locator = storage.accountRecordStorage.appendRecord(newAccount)(AccountCodec)
    val privateKeyLocator = storage.getPrivateKeyLocator(locator)

    privateKeyLocator.recordLocator.size shouldBe 33
  }

  "getAccount" should "return account name associated with address" in {

    val account = "account"
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = 1

    storage.accountIndex.existAccount(account) shouldBe false
    storage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address)

    storage.getAccount(address) shouldBe Some(account)
  }

  "getAccount" should "return None if there is no account associated with address" in {

    val account1 = "account"
    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val publicKey1 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey1 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose1 = 1

    val address2 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5bbbb"

    storage.accountIndex.existAccount(account1) shouldBe false
    storage.putNewAddress(account1, address1, purpose1, publicKey1, privateKey1) shouldBe Some(address1)

    storage.getAccount(address1) shouldBe Some(account1)
    storage.getAccount(address2) shouldBe None
  }


}
