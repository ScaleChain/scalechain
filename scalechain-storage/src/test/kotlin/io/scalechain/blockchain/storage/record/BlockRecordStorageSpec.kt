package io.scalechain.blockchain.storage.record

/**
  * Created by kangmo on 3/12/16.
  */

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.test.TestData.block1
import io.scalechain.blockchain.storage.test.TestData.block2
import io.scalechain.blockchain.storage.test.TestData.transaction1
import io.scalechain.blockchain.storage.test.TestData.transaction2
import org.apache.commons.io.FileUtils

/**
  * Created by kangmo on 11/2/15.
  */
class BlockRecordStorageSpec : FlatSpec(), Matchers {

  // Use record storage with maxFileSize 1M, instead of using BlockRecordStorage, which uses 100M file size limit.
  lateinit var rs : RecordStorage

  override fun beforeEach() {

    val testPath = File("./target/unittests-BlockRecordStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()
    rs = RecordStorage(
      testPath,
      BlockRecordStorage.FILE_PREFIX,
      maxFileSize = 1024 * 1024)

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    rs.close()
  }

  init {
    Storage.initialize()

    "readRecord" should "successfully read a block after a block was appended" {
      val B = BlockCodec
      val locator1 = rs.appendRecord(B, block1)
      val locator2 = rs.appendRecord(B, block2)
      rs.readRecord(B, locator1) shouldBe block1
      rs.readRecord(B, locator2) shouldBe block2
    }

    "readRecord" should "successfully read a transaction after a block was appended" {
      val T = TransactionCodec
      val locator1 = rs.appendRecord(T, transaction1())
      val locator2 = rs.appendRecord(T, transaction2())
      rs.readRecord(T, locator1) shouldBe transaction1()
      rs.readRecord(T, locator2) shouldBe transaction2()
    }

    "appendRecord" should "create a file if it hits the maximum file size" {
      val B = BlockCodec
      val locator1 = rs.appendRecord(B, block1)
      var locator2: FileRecordLocator = locator1

      // Loop until a file is created.
      while (locator2.fileIndex == locator1.fileIndex)
        locator2 = rs.appendRecord(B, block2)

      rs.files.size shouldBe 2
      rs.lastFileIndex() shouldBe 1
    }
  }
}
