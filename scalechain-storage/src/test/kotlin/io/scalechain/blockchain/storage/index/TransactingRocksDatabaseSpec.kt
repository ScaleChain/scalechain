package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest.{Suite, BeforeAndAfterEach}

class TransactingRocksDatabaseSpec extends KeyValueDatabaseTestTrait with KeyValueSeekTestTrait with KeyValuePrefixedSeekTestTrait with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null
  var txDb : TransactingRocksDatabase = null


  override def beforeEach() {

    val testPath = new File("./target/unittests-TransactingRocksDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    val rocksDB = new RocksDatabase(testPath)
    db = rocksDB
    txDb = new TransactingRocksDatabase( rocksDB )
    txDb.beginTransaction()
    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    txDb.close()
    db = null
    txDb = null
  }
}
