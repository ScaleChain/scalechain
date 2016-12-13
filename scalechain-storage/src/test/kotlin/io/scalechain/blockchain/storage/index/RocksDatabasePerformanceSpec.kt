package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import java.io.File
import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class RocksDatabasePerformanceSpec : KeyValueDatabasePerformanceTrait() {

  init {
    Storage.initialize()
    runTests()
  }
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
}
