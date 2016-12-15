package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactingRocksDatabaseKeyValueSpec : FlatSpec(), Matchers, DatabaseTestTraits {
  val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")

  lateinit var rocksDB : RocksDatabase
  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingRocksDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    rocksDB = RocksDatabase(testPath)
    txDb = TransactingRocksDatabase( rocksDB )
    txDb.beginTransaction()

    db = txDb

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}