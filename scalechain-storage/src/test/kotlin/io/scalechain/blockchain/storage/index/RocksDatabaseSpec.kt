package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// Currently RocksDB crashes while seeking a key and iterating (key,value) pairs.
@RunWith(KTestJUnitRunner::class)
class RocksDatabaseKeyValueSpec : KeyValueDatabaseTestTrait()  {

  init {
    Storage.initialize()
    runTests()
  }

  val testPath = File("./target/unittests-RocksDatabaseSpec")

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

class RocksDatabaseKeyValueSeekSpec : KeyValueSeekTestTrait()  {

  init {
    Storage.initialize()
    runTests()
  }

  val testPath = File("./target/unittests-RocksDatabaseSpec")

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

class RocksDatabaseKeyValuePrefixedSeekSpec : KeyValuePrefixedSeekTestTrait()  {

  init {
    Storage.initialize()
    runTests()
  }

  val testPath = File("./target/unittests-RocksDatabaseSpec")

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
