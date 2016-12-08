package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils

class TransactingRocksDatabaseKeyValueSpec : KeyValueDatabaseTestTrait()  {
  init {
    Storage.initialize()
    prepare()
    runTests()
  }
  val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")

  fun prepare() {
    FileUtils.deleteDirectory( testPath )
  }
  fun createDb() = RocksDatabase(testPath)
  fun createTxDb(rocksDB: RocksDatabase) = TransactingRocksDatabase( rocksDB )

  var rocksDB = createDb()
  override var db : KeyValueDatabase = rocksDB
  var txDb : TransactingRocksDatabase = createTxDb(rocksDB)

  override fun beforeEach() {
    rocksDB = createDb()
    db = rocksDB
    txDb = createTxDb(rocksDB)

    txDb.beginTransaction()
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
  }
}

class TransactingRocksDatabaseKeyValueSeekSpec : KeyValueSeekTestTrait()  {
  init {
    Storage.initialize()
    prepare()
    runTests()
  }
  val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")

  fun prepare() {
    FileUtils.deleteDirectory( testPath )
  }
  fun createDb() = RocksDatabase(testPath)
  fun createTxDb(rocksDB: RocksDatabase) = TransactingRocksDatabase( rocksDB )

  var rocksDB = createDb()
  override var db : KeyValueDatabase = rocksDB
  var txDb : TransactingRocksDatabase = createTxDb(rocksDB)

  override fun beforeEach() {
    rocksDB = createDb()
    db = rocksDB
    txDb = createTxDb(rocksDB)

    txDb.beginTransaction()
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
  }
}

class TransactingRocksDatabaseKeyValuePrefixedSeekSpec : KeyValuePrefixedSeekTestTrait()  {
  init {
    Storage.initialize()
    prepare()
    runTests()
  }
  val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")

  fun prepare() {
    FileUtils.deleteDirectory( testPath )
  }
  fun createDb() = RocksDatabase(testPath)
  fun createTxDb(rocksDB: RocksDatabase) = TransactingRocksDatabase( rocksDB )

  var rocksDB = createDb()
  override var db : KeyValueDatabase = rocksDB
  var txDb : TransactingRocksDatabase = createTxDb(rocksDB)

  override fun beforeEach() {
    rocksDB = createDb()
    db = rocksDB
    txDb = createTxDb(rocksDB)

    txDb.beginTransaction()
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
  }
}