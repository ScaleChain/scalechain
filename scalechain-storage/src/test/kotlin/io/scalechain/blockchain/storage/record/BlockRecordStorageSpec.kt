package io.scalechain.blockchain.storage.record

/**
  * Created by kangmo on 3/12/16.
  */

import java.io.File

import io.scalechain.blockchain.proto.{FileRecordLocator, RecordLocator}
import io.scalechain.blockchain.proto.codec.{TransactionCodec, BlockCodec}
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.index.{RocksDatabase, BlockDatabase}
import io.scalechain.blockchain.storage.test.TestData
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockRecordStorageSpec extends FlatSpec with BeforeAndAfterEach with Matchers {
  this: Suite =>

  Storage.initialize()
  import TestData._

  // Use record storage with maxFileSize 1M, instead of using BlockRecordStorage, which uses 100M file size limit.
  var rs : RecordStorage = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-BlockRecordStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()
    rs = new RecordStorage(
      testPath,
      BlockRecordStorage.FILE_PREFIX,
      maxFileSize = 1024 * 1024)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    rs.close()
  }

  "readRecord" should "successfully read a block after a block was appended" in {
    val B = BlockCodec
    val locator1 = rs.appendRecord(block1)(B)
    val locator2 = rs.appendRecord(block2)(B)
    rs.readRecord(locator1)(B) shouldBe block1
    rs.readRecord(locator2)(B) shouldBe block2
  }

  "readRecord" should "successfully read a transaction after a block was appended" in {
    val T = TransactionCodec
    val locator1 = rs.appendRecord(transaction1)(T)
    val locator2 = rs.appendRecord(transaction2)(T)
    rs.readRecord(locator1)(T) shouldBe transaction1
    rs.readRecord(locator2)(T) shouldBe transaction2
  }

  "appendRecord" should "create a new file if it hits the maximum file size" in {
    val B = BlockCodec
    val locator1 = rs.appendRecord(block1)(B)
    var locator2 : FileRecordLocator = locator1

    // Loop until a new file is created.
    while (locator2.fileIndex == locator1.fileIndex)
      locator2 = rs.appendRecord(block2)(B)

    rs.files.size shouldBe 2
    rs.lastFileIndex shouldBe 1
  }
}
