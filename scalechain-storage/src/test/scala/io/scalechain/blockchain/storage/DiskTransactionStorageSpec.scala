package io.scalechain.blockchain.storage

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util
import java.util.ArrayList

import io.scalechain.blockchain.proto.codec.{WalletTransactionDetailCodec}
import io.scalechain.blockchain.proto.{Hash}
import io.scalechain.blockchain.storage.TestData._
import io.scalechain.blockchain.storage.index.{TransactionDatabase, AccountDatabase}
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

  var accountStorage : DiskAccountStorage = null
  var rs : RecordStorage = null

  var transactionTestPath : File = null
  var accountTestPath : File = null
  var rsTestPath : File = null

  override def beforeEach() {

    transactionTestPath = new File("./target/unittests-DiskTransactionStorageSpec/")
    accountTestPath = new File("./target/unittests-DiskAccountStorageSpec/")

    FileUtils.deleteDirectory(transactionTestPath)
    FileUtils.deleteDirectory(accountTestPath)
    transactionTestPath.mkdir()
    accountTestPath.mkdir()

    rsTestPath = new File("./target/unittests-TransactionRecordStorageSpec/")
    FileUtils.deleteDirectory(rsTestPath)
    rsTestPath.mkdir()

    rs = new RecordStorage(
      rsTestPath,
      TransactionRecordStorage.FILE_PREFIX,
      TransactionRecordStorage.MAX_FILE_SIZE
    )

    DiskAccountStorage.columnFamilyName = new ArrayList[String]
    accountStorage = new DiskAccountStorage(accountTestPath, transactionTestPath)
    accountStorage.open

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    accountStorage.close()
    rs.close()
  }

  "getTransactionList" should "return transaction list associated with the given account" in {
    val account = walletTransactionDetail1.account
    val address = walletTransactionDetail1.address
    val publicKey = bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7")
    val privateKey = bytes("d8b7ee1319be14a5e2b0b89d5c3ba9d3dfc820e5944ae730e9f1875fa23355f9")
    val purpose = AccountDatabase.ADDRESS_UNKNOWN_PURPOSE

    accountStorage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address)

    val W = WalletTransactionDetailCodec
    val locator = rs.appendRecord(walletTransactionDetail1)(W)
    rs.readRecord(locator)(W) shouldBe walletTransactionDetail1

    val txid = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))

    accountStorage.putTransaction(account, txid, walletTransactionDetail1, TransactionDatabase.TRANSACTION_RECEIVE)

    val transactionList = accountStorage.getTransactionList(account, 10, 0, false).get
    transactionList.size shouldBe 1
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

    val txid2 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 10
      """))

    accountStorage.putTransaction(account1, txid1, walletTransactionDetail1, TransactionDatabase.TRANSACTION_RECEIVE)
    accountStorage.putTransaction(account2, txid2, walletTransactionDetail2, TransactionDatabase.TRANSACTION_RECEIVE)

    val accountArray = util.Arrays.asList(TransactionDatabase.WALLET_TRANSACTION_INFO + account1,
      TransactionDatabase.WALLET_TRANSACTION_INFO + account2)
    val path = Paths.get("./target/unittests-DiskTransactionStorageSpec/transaction-cf")
    Files.write(path, accountArray, Charset.forName("UTF-8"))

    val transactionList = accountStorage.getTransactionList("", 10, 0, false).get
    transactionList.size shouldBe 2
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

    accountStorage.putNewAddress(account, address, purpose, publicKey, privateKey) shouldBe Some(address)

    val W = WalletTransactionDetailCodec
    val locator = rs.appendRecord(walletTransactionDetail1)(W)
    rs.readRecord(locator)(W) shouldBe walletTransactionDetail1

    val txid = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))

    accountStorage.putTransaction(account, txid, walletTransactionDetail1, TransactionDatabase.TRANSACTION_RECEIVE)

    val transactionList = accountStorage.getTransactionList(account, 10, 0, false).get
    transactionList.size shouldBe 1
    transactionList(0).account shouldBe account
    transactionList(0).address shouldBe address

    accountStorage.getTransactionList(walletTransactionDetail2.account, 10, 0, false) shouldBe None
  }

}
