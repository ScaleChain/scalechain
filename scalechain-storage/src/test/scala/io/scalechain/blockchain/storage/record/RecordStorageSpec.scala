package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var rs : RecordStorage = null

  override def beforeEach() {
    val testPath = new File("./target/unittests-RecordStorageSpec")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()
    rs = new BlockRecordStorage(testPath)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    rs.close()
  }

  "files" should "" in {
  }

  "lastFile" should "" in {
  }

  "lastFileIndex" should "" in {
  }

  "newFile(blockFile)" should "" in {
  }

  "newFile" should "" in {
  }

  "addNewFile" should "" in {
  }

  "appendRecord" should "" in {
  }

  "readRecord" should "" in {
  }

}
