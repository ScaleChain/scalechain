package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.codec.{AccountCodec, AddressCodec}
import io.scalechain.blockchain.storage.{Storage, TestData}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 22..
  */
class AccountRecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()
  import TestData._

  var rs : RecordStorage = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-AccountRecordStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()
    rs = new RecordStorage(
      testPath,
      AccountRecordStorage.FILE_PREFIX + "test",
      AccountRecordStorage.MAX_FILE_SIZE
    )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    rs.close()
  }

  "readRecord" should "successfully read an account after an account was appended" in {
    val A = AccountCodec
    val locator1 = rs.appendRecord(account1)(A)
    rs.readRecord(locator1)(A) shouldBe account1
  }

  "readRecord" should "successfully read an address after an address was appended" in {
    val A = AddressCodec
    val locator1 = rs.appendRecord(address1)(A)
    rs.readRecord(locator1)(A) shouldBe address1
  }

  "appendRecord" should "create a new file if it hits the maximum file size" in {
    val AC = AccountCodec
    val AD = AddressCodec
    val locator1 = rs.appendRecord(account1)(AC)
    var locator2 : FileRecordLocator = locator1

    // Loop until a new file is created.
    while (locator2.fileIndex == locator1.fileIndex)
      locator2 = rs.appendRecord(address1)(AD)

    rs.files.size shouldBe 2
    rs.lastFileIndex shouldBe 1
  }

}
