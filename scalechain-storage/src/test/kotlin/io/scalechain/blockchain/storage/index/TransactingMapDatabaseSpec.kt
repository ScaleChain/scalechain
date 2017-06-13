package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.Storage
import org.junit.Ignore
import org.junit.runner.RunWith
import java.io.File

@RunWith(KTestJUnitRunner::class)
@Ignore // MapDatabase is too slow. Will enable this case after optimizing MapDatabase.
class TransactingMapDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {
  val testPath = File("./build/unittests-TransactingMapDatabaseSpec")

  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingKeyValueDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    db = MapDatabase(testPath)
    txDb = db.transacting()
    txDb.beginTransaction()

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    db.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}