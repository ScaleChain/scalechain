package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

class TransactingRocksDatabasePerformanceSpec : FlatSpec with KeyValueDatabasePerformanceTrait with Matchers with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null
  var transactingDB : TransactingRocksDatabase = null

  override fun beforeEach() {

    val testPath = File("./target/unittests-RocksDatabasePerformanceSpec")
    FileUtils.deleteDirectory( testPath )
    transactingDB = TransactingRocksDatabase( RocksDatabase( testPath ) )
    transactingDB.beginTransaction()
    db = transactingDB

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    transactingDB.commitTransaction()
    db.close()
  }
}
