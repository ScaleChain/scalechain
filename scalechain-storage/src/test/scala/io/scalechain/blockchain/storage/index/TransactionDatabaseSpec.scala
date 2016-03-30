package io.scalechain.blockchain.storage.index

import java.io.File
import java.util.ArrayList

import io.scalechain.blockchain.proto.{WalletTransactionInfo, RecordLocator, FileRecordLocator, Hash}
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.storage.Storage
import io.scalechain.util.HexUtil._
import org.apache.commons.io.FileUtils
import org.scalatest._
import io.scalechain.blockchain.storage.TestData._

/**
  * Created by mijeong on 2016. 3. 30..
  */
class TransactionDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CodecTestUtil {
  this: Suite =>

  Storage.initialize()

  var db : TransactionDatabase = null

  override def beforeEach() {
    val testPath = new File("./target/unittests-TransactionDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = new TransactionDatabase( new RocksDatabaseWithCF(testPath, new ArrayList[String]()))

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  "putTransaction/getTransactionLocator" should "successfully put/get transaction info" in {
    val address = walletTransactionDetail1.address
    val txid = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator = FileRecordLocator(1, RecordLocator(0, 80))
    val walletTransactionInfo = WalletTransactionInfo (
      1, Some(txLocator)
    )

    db.putTransaction(address, txid, walletTransactionInfo)
    db.getTransactionLocator(address, txid) shouldBe Some(txLocator)
  }

  "getTransactionLocator" should "return None if there is no the given txid" in {
    val address = walletTransactionDetail1.address
    val txid1 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator = FileRecordLocator(1, RecordLocator(0, 80))
    val walletTransactionInfo = WalletTransactionInfo (
      1, Some(txLocator)
    )

    val txid2 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 10
      """))

    db.putTransaction(address, txid1, walletTransactionInfo)
    db.getTransactionLocator(address, txid1) shouldBe Some(txLocator)
    db.getTransactionLocator(address, txid2) shouldBe None
  }

  "putUnspentTransaction/getUnspentTransactionLocator" should "successfully put/get unspent transaction info" in {

    val address = walletTransactionDetail1.address
    val txid = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator = FileRecordLocator(1, RecordLocator(0, 80))

    db.putUnspentTransaction(address, txid, txLocator)
    db.getUnspentTransactionLocator(address, txid) shouldBe Some(txLocator)
  }

  "getUnspentTransactionLocator" should "return None if there is no the given txid" in {

    val address = walletTransactionDetail1.address
    val txid1 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 7d
      """))
    val txLocator = FileRecordLocator(1, RecordLocator(0, 80))

    val txid2 = Hash(bytes(
      """
      99 84 5f d8 40 ad 2c c4  d6 f9 3f af b8 b0 72 d1
      88 82 1f 55 d9 29 87 72  41 51 75 c4 56 f3 07 10
      """))

    db.putUnspentTransaction(address, txid1, txLocator)
    db.getUnspentTransactionLocator(address, txid1) shouldBe Some(txLocator)
    db.getUnspentTransactionLocator(address, txid2) shouldBe None
  }

}
