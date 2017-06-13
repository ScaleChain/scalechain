package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactingRocksDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {
  val testPath = File("./build/unittests-TransactingRocksDatabaseSpec")

  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingKeyValueDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    db = RocksDatabase(testPath)
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