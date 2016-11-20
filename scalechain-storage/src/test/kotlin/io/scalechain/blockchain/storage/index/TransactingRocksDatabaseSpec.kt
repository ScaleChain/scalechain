package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest.{Suite, BeforeAndAfterEach}

class TransactingRocksDatabaseSpec : KeyValueDatabaseTestTrait with KeyValueSeekTestTrait with KeyValuePrefixedSeekTestTrait with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null
  var txDb : TransactingRocksDatabase = null


  override fun beforeEach() {

    val testPath = File("./target/unittests-TransactingRocksDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    val rocksDB = RocksDatabase(testPath)
    db = rocksDB
    txDb = TransactingRocksDatabase( rocksDB )
    txDb.beginTransaction()
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
    db = null
    txDb = null
  }
}
