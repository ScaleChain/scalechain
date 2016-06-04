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

  var db : BlockDatabase = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-BlockDatabaseWithRocks")
    FileUtils.deleteDirectory( testPath )
    db = new BlockDatabase( new RocksDatabase( testPath ) )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }
}
