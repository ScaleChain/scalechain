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
class TransactingRocksDatabaseKeyValueSpec : FlatSpec(), Matchers, KeyValueDatabaseTestTrait, KeyValueSeekTestTrait, KeyValuePrefixedSeekTestTrait {
  val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")

  lateinit var rocksDB : RocksDatabase
  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingRocksDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()

    rocksDB = RocksDatabase(testPath)
    db = rocksDB
    txDb = TransactingRocksDatabase( rocksDB )

    txDb.beginTransaction()
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
  }

  override fun addTests() {
    super<KeyValueDatabaseTestTrait>.addTests()
    super<KeyValueSeekTestTrait>.addTests()
    super<KeyValuePrefixedSeekTestTrait>.addTests()
  }

  init {
    Storage.initialize()
    addTests()
  }
}