package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.codec._
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.TestData._
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 30..
  */
class TransactionRecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()
  var rs : RecordStorage = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-TransactionRecordStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    rs = new RecordStorage(
      testPath,
      TransactionRecordStorage.FILE_PREFIX,
      TransactionRecordStorage.MAX_FILE_SIZE
    )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    rs.close()
  }

  "readRecord" should "successfully read a transaction after a transaction was appended" in {
    val W = WalletTransactionCodec
    val locator1 = rs.appendRecord(walletTransaction)(W)
    rs.readRecord(locator1)(W) shouldBe walletTransaction
  }

  "readRecord" should "successfully read a transaction detail after a transaction detail was appended" in {
    val W = WalletTransactionDetailCodec
    val locator1 = rs.appendRecord(walletTransactionDetail1)(W)
    rs.readRecord(locator1)(W) shouldBe walletTransactionDetail1
  }

  "appendRecord" should "create a new file if it hits the maximum file size" in {
    val WTH = WalletTransactionHeaderCodec
    val WTD = WalletTransactionDetailCodec
    val locator1 = rs.appendRecord(walletTransactionHeader)(WTH)
    var locator2 : FileRecordLocator = locator1

    // Loop until a new file is created.
    while (locator2.fileIndex == locator1.fileIndex)
      locator2 = rs.appendRecord(walletTransactionDetail1)(WTD)

    rs.files.size shouldBe 2
    rs.lastFileIndex shouldBe 1
  }


}
