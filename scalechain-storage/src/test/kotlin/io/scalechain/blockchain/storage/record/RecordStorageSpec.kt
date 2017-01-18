package io.scalechain.blockchain.storage.record

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.BlockStorageException
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.codec.OneByteCodec
import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class RecordStorageSpec : FlatSpec(), Matchers {


  lateinit var rs: RecordStorage

  val testPath = File("./target/unittests-RecordStorageSpec")
  fun openRecordStorage() = RecordStorage(testPath, filePrefix = "blk", maxFileSize = 12)

  override fun beforeEach() {

    testPath.deleteRecursively()
    testPath.mkdir()

    // Test with maximum file size, 12 bytes.
    // We will create multiple files whose size is 12.
    rs = openRecordStorage()

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    rs.close()
  }

  fun expectFileCount(count: Int) {
    val fileIndex = count - 1
    rs.files.size shouldBe count
    rs.lastFileIndex() shouldBe (fileIndex)
    rs.lastFile().path.getName() shouldBe BlockFileName("blk", fileIndex).toString()
  }

  init {
    Storage.initialize()

    "readRecord" should "throw an exception if the file number is incorrect " {
      val R = FileNumberCodec
      val thrown1 = shouldThrow<BlockStorageException> {
        // fileIndex = -1 should throw an exception
        rs.readRecord(R, FileRecordLocator(fileIndex = -1, recordLocator = RecordLocator(offset=0, size=10)))
      }
      thrown1.code shouldBe ErrorCode.InvalidFileNumber

      val thrown2 = shouldThrow<BlockStorageException> {
        // fileIndex out of bounds(0, as there is only one file in the beginning) should throw an exception
        rs.readRecord(R, FileRecordLocator(fileIndex = 1, recordLocator = RecordLocator(offset=0, size=10)))
      }
      thrown2.code shouldBe ErrorCode.InvalidFileNumber

    }

    "readRecord" should "read existing records when the storage opens with existing files" {
      val R = FileNumberCodec

      val locator1 = rs.appendRecord(R, FileNumber(1))
      val locator2 = rs.appendRecord(R, FileNumber(2))
      val locator3 = rs.appendRecord(R, FileNumber(3))
      val locator4 = rs.appendRecord(R, FileNumber(4))

      expectFileCount(2)

      rs.flush()
      rs.close()

      rs = openRecordStorage()
      expectFileCount(2)

      rs.readRecord(R, locator1) shouldBe FileNumber(1)
      rs.readRecord(R, locator2) shouldBe FileNumber(2)
      rs.readRecord(R, locator3) shouldBe FileNumber(3)
      rs.readRecord(R, locator4) shouldBe FileNumber(4)
    }

    "appendRecord" should "append at the end of the last file when the storage opens with existing files" {
      val R = FileNumberCodec

      val locator1 = rs.appendRecord(R, FileNumber(1))
      val locator2 = rs.appendRecord(R, FileNumber(2))
      val locator3 = rs.appendRecord(R, FileNumber(3))
      val locator4 = rs.appendRecord(R, FileNumber(4))

      rs.flush()
      rs.close()

      rs = openRecordStorage()

      val locator5 = rs.appendRecord(R, FileNumber(5))

      rs.readRecord(R, locator1) shouldBe FileNumber(1)
      rs.readRecord(R, locator2) shouldBe FileNumber(2)
      rs.readRecord(R, locator3) shouldBe FileNumber(3)
      rs.readRecord(R, locator4) shouldBe FileNumber(4)
      rs.readRecord(R, locator5) shouldBe FileNumber(5)
    }

    "readRecord" should "read a record appended" {
      val R = FileNumberCodec

      rs.files.size shouldBe 1
      rs.lastFileIndex() shouldBe 0

      val locator = rs.appendRecord(R, FileNumber(1))
      locator.fileIndex shouldBe 0

      rs.files.size shouldBe 1
      rs.lastFileIndex() shouldBe 0

      rs.readRecord(R, locator) shouldBe FileNumber(1)
    }

    "readRecord" should "read multiple records" {
      val R = FileNumberCodec

      expectFileCount(1)

      val locator1 = rs.appendRecord(R, FileNumber(1))
      locator1.fileIndex shouldBe 0

      expectFileCount(1)

      val locator2 = rs.appendRecord(R, FileNumber(2))
      val locator3 = rs.appendRecord(R, FileNumber(3))

      locator3.fileIndex shouldBe 0

      expectFileCount(1)

      rs.readRecord(R, locator1) shouldBe FileNumber(1)
      rs.readRecord(R, locator2) shouldBe FileNumber(2)
      rs.readRecord(R, locator3) shouldBe FileNumber(3)
    }

    "appendRecord" should "create a new file if we hit the max file size limit. case 1 : no remaining space for the first file" {
      val R = FileNumberCodec

      expectFileCount(1)

      val locator1 = rs.appendRecord(R, FileNumber(1))
      val locator2 = rs.appendRecord(R, FileNumber(2))
      val locator3 = rs.appendRecord(R, FileNumber(3))

      expectFileCount(1)

      // hit the limit. a new file should have been created.
      val locator4 = rs.appendRecord(R, FileNumber(4))
      locator4.fileIndex shouldBe 1

      expectFileCount(2)

      rs.readRecord(R, locator1) shouldBe FileNumber(1)
      rs.readRecord(R, locator2) shouldBe FileNumber(2)
      rs.readRecord(R, locator3) shouldBe FileNumber(3)
      rs.readRecord(R, locator4) shouldBe FileNumber(4)
    }


    "appendRecord" should "create a new file if we hit the max file size limit. case 2 : some remaining space for the first file" {

      val R = FileNumberCodec

      expectFileCount(1)

      val locator1 = rs.appendRecord(R, FileNumber(1))
      val locator2 = rs.appendRecord(R, FileNumber(2))
      val locator3 = rs.appendRecord(OneByteCodec, OneByte('a'.toByte()))

      expectFileCount(1)

      // hit the limit. a file should have been created.
      val locator4 = rs.appendRecord(R,FileNumber(4))
      locator4.fileIndex shouldBe 1

      expectFileCount(2)

      rs.readRecord(R,locator1) shouldBe FileNumber(1)
      rs.readRecord(R,locator2) shouldBe FileNumber(2)
      rs.readRecord(OneByteCodec,locator3) shouldBe OneByte('a'.toByte())
      rs.readRecord(R,locator4) shouldBe FileNumber(4)
    }

    "newFile()" should "create a new file without adding to the files sequence" {
      expectFileCount(1)

      val newFile = rs.newFile()

      expectFileCount(1)

      newFile.path.getName() shouldBe BlockFileName("blk", 1).toString()
    }

    "newFile(blockFile)" should "should create a new record file." {
      val fileName = BlockFileName("abc", 1).toString()
      val filePath = "./target/" + fileName

      val f = File(filePath)
      f.delete()

      val newFile = rs.newFile(f)
      newFile.path.getName() shouldBe fileName
    }

    "newFile(blockFile)" should "should throw BlockStorageException if it hits size limit. case 1 : no remaining space for the first file." {
      val R = FileNumberCodec

      val f = File("./target/" + BlockFileName("abc", 1))
      f.delete()

      val newFile = rs.newFile(f)
      val locator1 = newFile.appendRecord(R, FileNumber(1))
      val locator2 = newFile.appendRecord(R, FileNumber(2))
      val locator3 = newFile.appendRecord(R, FileNumber(3))

      newFile.readRecord(R, locator1) shouldBe FileNumber(1)
      newFile.readRecord(R, locator2) shouldBe FileNumber(2)
      newFile.readRecord(R, locator3) shouldBe FileNumber(3)

      val thrown = shouldThrow<BlockStorageException> {
        newFile.appendRecord(R, FileNumber(4))
      }
      thrown.code shouldBe ErrorCode.OutOfFileSpace
    }

    "newFile(blockFile)" should "should throw BlockStorageException if it hits size limit. case 2 : some remaining space for the first file." {
      val R = FileNumberCodec

      val f = File("./target/" + BlockFileName("abc", 1))
      f.delete()

      val newFile = rs.newFile(f)
      val locator1 = newFile.appendRecord(R, FileNumber(1))
      val locator2 = newFile.appendRecord(R, FileNumber(2))
      val locator3 = newFile.appendRecord(OneByteCodec, OneByte('a'.toByte()))

      newFile.readRecord(R, locator1) shouldBe FileNumber(1)
      newFile.readRecord(R, locator2) shouldBe FileNumber(2)
      newFile.readRecord(OneByteCodec, locator3) shouldBe OneByte('a'.toByte())

      val thrown = shouldThrow<BlockStorageException> {
        newFile.appendRecord(R, FileNumber(4))
      }
      thrown.code shouldBe ErrorCode.OutOfFileSpace
    }

    "addNewFile" should "should increase the file count." {
      expectFileCount(1)

      rs.addNewFile()

      expectFileCount(2)
    }
  }
}
