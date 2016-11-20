package io.scalechain.blockchain.storage.index


import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockDatabaseWithRocks : BlockDatabaseTestTrait with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var blockDb : BlockDatabase = null
  implicit var db : KeyValueDatabase = null

  override fun beforeEach() {

    val testPath = File("./target/unittests-BlockDatabaseWithRocks")
    FileUtils.deleteDirectory( testPath )
    db = RocksDatabase(testPath)
    blockDb = BlockDatabase() {}


    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
    db = null
    blockDb = null
  }
}
