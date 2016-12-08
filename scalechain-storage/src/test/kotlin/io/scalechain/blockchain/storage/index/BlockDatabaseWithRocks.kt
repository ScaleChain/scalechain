package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import java.io.File
import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class BlockDatabaseWithRocks : BlockDatabaseTestTrait() {

  init {
    Storage.initialize()
  }

  val testPath = File("./target/unittests-BlockDatabaseWithRocks")


  lateinit override var db : KeyValueDatabase
  lateinit override var blockDb : BlockDatabase

  override fun beforeEach() {
    FileUtils.deleteDirectory( testPath )

    db = RocksDatabase(testPath)
    blockDb = object : BlockDatabase {}


    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }
}
