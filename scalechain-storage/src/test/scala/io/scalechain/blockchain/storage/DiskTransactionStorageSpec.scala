package io.scalechain.blockchain.storage

import java.io.File
import java.util.ArrayList

import io.scalechain.blockchain.proto.codec.{WalletTransactionDetailCodec}
import io.scalechain.blockchain.proto.{WalletTransactionInfo, Hash}
import io.scalechain.blockchain.storage.TestData._
import io.scalechain.blockchain.storage.index.AccountDatabase
import io.scalechain.blockchain.storage.record.{TransactionRecordStorage, RecordStorage}
import io.scalechain.util.HexUtil._
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 31..
  */
class DiskTransactionStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var storage : DiskTransactionStorage = null
  var accountStorage : DiskAccountStorage = null
  var rs : RecordStorage = null

  var testPath : File = null
  var accountTestPath : File = null
  var rsTestPath : File = null

  override def beforeEach() {

    testPath = new File("./target/unittests-DiskTransactionStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    accountTestPath = new File("./target/unittests-DiskAccountStorageSpec/")
    FileUtils.deleteDirectory(accountTestPath)
    accountTestPath.mkdir()

    rsTestPath = new File("./target/unittests-TransactionRecordStorageSpec/")
    FileUtils.deleteDirectory(rsTestPath)
    rsTestPath.mkdir()

    rs = new RecordStorage(
      rsTestPath,
      TransactionRecordStorage.FILE_PREFIX,
      TransactionRecordStorage.MAX_FILE_SIZE
    )

    DiskTransactionStorage.columnFamilyName = new ArrayList[String]
    storage = new DiskTransactionStorage(testPath)
    storage.open

    DiskAccountStorage.columnFamilyName = new ArrayList[String]
    accountStorage = new DiskAccountStorage(accountTestPath)
    accountStorage.open

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
    accountStorage.close()
    rs.close()
  }

  "getTransactionList" should "return transaction list associated with the given account" in {
    val account = walletTransactionDetail1.account
    val address = walletTransactionDetail1.address
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    accountStorage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address1)

    val W = WalletTransactionDetailCodec
    val locator = rs.appendRecord(walletTransactionDetail1)(W)
    rs.readRecord(locator)(W) shouldBe walletTransactionDetail1

    val txid = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator = locator
    val walletTransactionInfo = WalletTransactionInfo (
      0, Some(txLocator)
    )

    storage.putTransaction(address, txid, walletTransactionInfo)

    val transactionList = storage.getTransactionList(account).get
    transactionList.size() shouldBe 1
    transactionList(0).account shouldBe account
    transactionList(0).address shouldBe address
  }

  "getTransactionList" should "return all transaction list associated with wallet" in {
    val account1 = walletTransactionDetail1.account
    val address1 = walletTransactionDetail1.address
    val publicKey1 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey1 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose1 = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    val account2 = walletTransactionDetail2.account
    val address2 = walletTransactionDetail2.address
    val publicKey2 = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863d1234")
    val privateKey2 = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa2331234")
    val purpose2 = AccountDatabase.ADDRESS_RECEIVED_PURPOSE

    accountStorage.putNewAddress(account1, address1, purpose1, publicKey1, privateKey1) shouldBe Some(address1)
    accountStorage.putNewAddress(account2, address2, purpose2, publicKey2, privateKey2) shouldBe Some(address2)

    val W = WalletTransactionDetailCodec
    val locator1 = rs.appendRecord(walletTransactionDetail1)(W)
    rs.readRecord(locator1)(W) shouldBe walletTransactionDetail1
    val locator2 = rs.appendRecord(walletTransactionDetail2)(W)
    rs.readRecord(locator2)(W) shouldBe walletTransactionDetail2

    val txid1 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator1 = locator1
    val walletTransactionInfo1 = WalletTransactionInfo (
      0, Some(txLocator1)
    )

    val txid2 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 10
      """))
    val txLocator2 = locator2
    val walletTransactionInfo2 = WalletTransactionInfo (
      0, Some(txLocator2)
    )

    storage.putTransaction(address1, txid1, walletTransactionInfo1)
    storage.putTransaction(address2, txid2, walletTransactionInfo2)

    val transactionList = storage.getTransactionList("").get
    transactionList.size() shouldBe 2
    transactionList(0).account shouldBe account1
    transactionList(0).address shouldBe address1
    transactionList(1).account shouldBe account2
    transactionList(1).address shouldBe address2
  }

  "getTransactionList" should "return None if there is no transaction associated with the given account or wallet" in {

    val account = walletTransactionDetail1.account
    val address = walletTransactionDetail1.address
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    accountStorage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address1)

    val W = WalletTransactionDetailCodec
    val locator = rs.appendRecord(walletTransactionDetail1)(W)
    rs.readRecord(locator)(W) shouldBe walletTransactionDetail1

    val txid = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator = locator
    val walletTransactionInfo = WalletTransactionInfo (
      0, Some(txLocator)
    )

    storage.putTransaction(address, txid, walletTransactionInfo)

    val transactionList = storage.getTransactionList(account).get
    transactionList.size() shouldBe 1
    transactionList(0).account shouldBe account
    transactionList(0).address shouldBe address

    storage.getTransactionList(walletTransactionDetail2.account) shouldBe None
  }

}
