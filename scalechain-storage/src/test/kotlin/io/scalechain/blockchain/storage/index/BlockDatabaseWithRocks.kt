package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import java.io.File
import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class BlockDatabaseWithRocks : BlockDatabaseTestTrait() {

  init {
    Storage.initialize()
    addTests()
  }

  val testPath = File("./build/unittests-BlockDatabaseWithRocks")


  lateinit override var db : KeyValueDatabase
  lateinit override var blockDb : BlockDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    db = DatabaseFactory.create(testPath)
    blockDb = object : BlockDatabase {}


    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }
}
