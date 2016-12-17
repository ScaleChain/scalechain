package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// Currently RocksDB crashes while seeking a key and iterating (key,value) pairs.
@RunWith(KTestJUnitRunner::class)
class RocksDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {

  val testPath = File("./target/unittests-RocksDatabaseSpec")

  lateinit override var db : KeyValueDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    db = RocksDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}
