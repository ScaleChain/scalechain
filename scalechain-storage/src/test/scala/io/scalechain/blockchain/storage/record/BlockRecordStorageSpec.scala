package io.scalechain.blockchain.storage.record

/**
  * Created by kangmo on 3/12/16.
  */

import java.io.File

import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.index.{RocksDatabase, BlockDatabase}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockRecordStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var rs : BlockRecordStorage = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-BlockRecordStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()
    rs = new BlockRecordStorage(testPath)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    rs.close()
  }

  "readRecord" should "successfully read a block after a block was appended" in {

  }

  "readRecord" should "successfully read a transaction after a block was appended" in {
  }
}
