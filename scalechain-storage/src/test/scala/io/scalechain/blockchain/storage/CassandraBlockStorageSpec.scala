package io.scalechain.blockchain.storage

import java.io.File

import org.apache.commons.io.FileUtils
import org.scalatest.{Suite, BeforeAndAfterEach}

class CassandraBlockStorageSpec extends BlockStorageTestTrait with BeforeAndAfterEach {
  this: Suite =>

  import TestData._

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var diskBlockStorage: CassandraBlockStorage = null
  var storage: BlockStorage = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-CassandraBlockStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    diskBlockStorage = new CassandraBlockStorage(testPath)
    storage = diskBlockStorage

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
  }
}