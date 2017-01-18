package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactingRocksDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {
  val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")

  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingKeyValueDatabase
  lateinit var rocksDB : RocksDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    rocksDB = RocksDatabase(testPath)
    txDb = rocksDB.transacting()
    txDb.beginTransaction()
    db = txDb

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    rocksDB.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}