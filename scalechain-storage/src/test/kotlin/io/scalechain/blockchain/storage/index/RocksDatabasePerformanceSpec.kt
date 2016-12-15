package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File
import io.scalechain.blockchain.storage.Storage
import io.scalechain.test.BeforeAfterEach
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class RocksDatabasePerformanceSpec : FlatSpec(), Matchers, KeyValueDatabasePerformanceTrait {

  val testPath = File("./target/unittests-RocksDatabasePerformanceSpec")

  lateinit override var db : KeyValueDatabase

  override fun beforeEach() {
    FileUtils.deleteDirectory( testPath )

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
