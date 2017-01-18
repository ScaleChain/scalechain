package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactingRocksDatabasePerformanceSpec : FlatSpec(), Matchers, KeyValueDatabasePerformanceTrait {
  val testPath = File("./target/unittests-DatabasePerformanceSpec")

  lateinit var transactingDB : TransactingKeyValueDatabase
  lateinit override var db : KeyValueDatabase
  lateinit var rocksDB : RocksDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    rocksDB = RocksDatabase( testPath )
    transactingDB = rocksDB.transacting()
    transactingDB.beginTransaction()
    db = transactingDB

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    transactingDB.commitTransaction()
    rocksDB.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}
