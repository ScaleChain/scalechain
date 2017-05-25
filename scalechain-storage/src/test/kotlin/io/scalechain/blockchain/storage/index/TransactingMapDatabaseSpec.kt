package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.Storage
import org.junit.Ignore
import org.junit.runner.RunWith
import java.io.File

@RunWith(KTestJUnitRunner::class)
//@Ignore // MapDatabase is too slow. Will enable this case after optimizing MapDatabase.
class TransactingMapDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {
  val testPath = File("./build/unittests-TransactingMapDatabaseSpec")

  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingKeyValueDatabase
  lateinit var mapDB : MapDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    mapDB = MapDatabase(testPath)
    txDb = mapDB.transacting()
    txDb.beginTransaction()
    db = txDb

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    mapDB.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}