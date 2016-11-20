package io.scalechain.blockchain.storage.index


import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockDatabaseWithRocks extends BlockDatabaseTestTrait with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var blockDb : BlockDatabase = null
  implicit var db : KeyValueDatabase = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-BlockDatabaseWithRocks")
    FileUtils.deleteDirectory( testPath )
    db = new RocksDatabase(testPath)
    blockDb = new BlockDatabase() {}


    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
    db = null
    blockDb = null
  }
}
