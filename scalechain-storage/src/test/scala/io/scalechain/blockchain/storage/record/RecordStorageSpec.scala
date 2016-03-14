package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.{ErrorCode, BlockStorageException}
import io.scalechain.blockchain.proto.{OneByte, FileNumber}
import io.scalechain.blockchain.proto.codec.{OneByteCodec, FileNumberCodec}
import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._
import Matchers._
import scodec.codecs._

/**
  * Created by kangmo on 11/2/15.
  */
class RecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var rs : RecordStorage = null

  val testPath = new File("./target/unittests-RecordStorageSpec")
  def openRecordStorage() = new RecordStorage(testPath, filePrefix="blk", maxFileSize=12)

  override def beforeEach() {
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    // Test with maximum file size, 12 bytes.
    // We will create multiple files whose size is 12.
    rs = openRecordStorage()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    rs.close()
  }

  "readRecord" should "read existing records when the storage opens with existing files" in {
    val R = FileNumberCodec

    val locator1 = rs.appendRecord(FileNumber(1))(R)
    val locator2 = rs.appendRecord(FileNumber(2))(R)
    val locator3 = rs.appendRecord(FileNumber(3))(R)
    val locator4 = rs.appendRecord(FileNumber(4))(R)

    expectFileCount(2)

    rs.close()

    rs = openRecordStorage()
    expectFileCount(2)

    rs.readRecord(locator1)(R) shouldBe FileNumber(1)
    rs.readRecord(locator2)(R) shouldBe FileNumber(2)
    rs.readRecord(locator3)(R) shouldBe FileNumber(3)
    rs.readRecord(locator4)(R) shouldBe FileNumber(4)
  }

  "appendRecord" should "append at the end of the last file when the storage opens with existing files" in {
    val R = FileNumberCodec

    val locator1 = rs.appendRecord(FileNumber(1))(R)
    val locator2 = rs.appendRecord(FileNumber(2))(R)
    val locator3 = rs.appendRecord(FileNumber(3))(R)
    val locator4 = rs.appendRecord(FileNumber(4))(R)

    rs.close()

    rs = openRecordStorage()

    val locator5 = rs.appendRecord(FileNumber(5))(R)

    rs.readRecord(locator1)(R) shouldBe FileNumber(1)
    rs.readRecord(locator2)(R) shouldBe FileNumber(2)
    rs.readRecord(locator3)(R) shouldBe FileNumber(3)
    rs.readRecord(locator4)(R) shouldBe FileNumber(4)
    rs.readRecord(locator5)(R) shouldBe FileNumber(5)
  }

  "readRecord" should "read a record appended" in {
    val R = FileNumberCodec

    rs.files.size shouldBe 1
    rs.lastFileIndex shouldBe 0

    val locator = rs.appendRecord(FileNumber(1))(R)
    locator.fileIndex shouldBe 0

    rs.files.size shouldBe 1
    rs.lastFileIndex shouldBe 0

    rs.readRecord(locator)(R) shouldBe FileNumber(1)
  }

  "readRecord" should "read multiple records" in {
    val R = FileNumberCodec

    expectFileCount(1)

    val locator1 = rs.appendRecord(FileNumber(1))(R)
    locator1.fileIndex shouldBe 0

    expectFileCount(1)

    val locator2 = rs.appendRecord(FileNumber(2))(R)
    val locator3 = rs.appendRecord(FileNumber(3))(R)

    locator3.fileIndex shouldBe 0

    expectFileCount(1)

    rs.readRecord(locator1)(R) shouldBe FileNumber(1)
    rs.readRecord(locator2)(R) shouldBe FileNumber(2)
    rs.readRecord(locator3)(R) shouldBe FileNumber(3)
  }

  def expectFileCount(count : Int) = {
    val fileIndex = count-1
    rs.files.size shouldBe count
    rs.lastFileIndex shouldBe (fileIndex)
    rs.lastFile.path.getName shouldBe BlockFileName("blk", fileIndex)
  }

  "appendRecord" should "create a new file if we hit the max file size limit. case 1 : no remaining space for the first file" in {
    val R = FileNumberCodec

    expectFileCount(1)

    val locator1 = rs.appendRecord(FileNumber(1))(R)
    val locator2 = rs.appendRecord(FileNumber(2))(R)
    val locator3 = rs.appendRecord(FileNumber(3))(R)

    expectFileCount(1)

    // hit the limit. a new file should have been created.
    val locator4 = rs.appendRecord(FileNumber(4))(R)
    locator4.fileIndex shouldBe 1

    expectFileCount(2)

    rs.readRecord(locator1)(R) shouldBe FileNumber(1)
    rs.readRecord(locator2)(R) shouldBe FileNumber(2)
    rs.readRecord(locator3)(R) shouldBe FileNumber(3)
    rs.readRecord(locator4)(R) shouldBe FileNumber(4)
  }


  "appendRecord" should "create a new file if we hit the max file size limit. case 2 : some remaining space for the first file" in {

    val R = FileNumberCodec

    expectFileCount(1)

    val locator1 = rs.appendRecord(FileNumber(1))(R)
    val locator2 = rs.appendRecord(FileNumber(2))(R)
    val locator3 = rs.appendRecord(OneByte('a'))(OneByteCodec)

    expectFileCount(1)

    // hit the limit. a new file should have been created.
    val locator4 = rs.appendRecord(FileNumber(4))(R)
    locator4.fileIndex shouldBe 1

    expectFileCount(2)

    rs.readRecord(locator1)(R) shouldBe FileNumber(1)
    rs.readRecord(locator2)(R) shouldBe FileNumber(2)
    rs.readRecord(locator3)(OneByteCodec) shouldBe OneByte('a')
    rs.readRecord(locator4)(R) shouldBe FileNumber(4)
  }

  "newFile()" should "create a new file without adding to the files sequence" in {
    expectFileCount(1)

    val newFile = rs.newFile

    expectFileCount(1)

    newFile.path.getName shouldBe BlockFileName("blk", 1)
  }

  "newFile(blockFile)" should "should create a new record file." in {
    val R = FileNumberCodec
    val fileName = BlockFileName("abc", 1)
    val filePath = "./target/"+fileName

    val f = new File(filePath)
    f.delete()

    val newFile = rs.newFile(f)
    newFile.path.getName shouldBe fileName
  }

  "newFile(blockFile)" should "should throw BlockStorageException if it hits size limit. case 1 : no remaining space for the first file." in {
    val R = FileNumberCodec

    val f = new File("./target/"+BlockFileName("abc", 1))
    f.delete()

    val newFile = rs.newFile(f)
    val locator1 = newFile.appendRecord(FileNumber(1))(R)
    val locator2 = newFile.appendRecord(FileNumber(2))(R)
    val locator3 = newFile.appendRecord(FileNumber(3))(R)

    newFile.readRecord(locator1)(R) shouldBe FileNumber(1)
    newFile.readRecord(locator2)(R) shouldBe FileNumber(2)
    newFile.readRecord(locator3)(R) shouldBe FileNumber(3)

    val thrown = the [BlockStorageException] thrownBy {
      newFile.appendRecord(FileNumber(4))(R)
    }
    thrown.code shouldBe ErrorCode.OutOfFileSpace
  }

  "newFile(blockFile)" should "should throw BlockStorageException if it hits size limit. case 2 : some remaining space for the first file." in {
    val R = FileNumberCodec

    val f = new File("./target/"+BlockFileName("abc", 1))
    f.delete()

    val newFile = rs.newFile(f)
    val locator1 = newFile.appendRecord(FileNumber(1))(R)
    val locator2 = newFile.appendRecord(FileNumber(2))(R)
    val locator3 = newFile.appendRecord(OneByte('a'))(OneByteCodec)

    newFile.readRecord(locator1)(R) shouldBe FileNumber(1)
    newFile.readRecord(locator2)(R) shouldBe FileNumber(2)
    newFile.readRecord(locator3)(OneByteCodec) shouldBe OneByte('a')

    val thrown = the [BlockStorageException] thrownBy {
      newFile.appendRecord(FileNumber(4))(R)
    }
    thrown.code shouldBe ErrorCode.OutOfFileSpace
  }

  "addNewFile" should "should increase the file count." in {
    expectFileCount(1)

    rs.addNewFile

    expectFileCount(2)
  }
}
