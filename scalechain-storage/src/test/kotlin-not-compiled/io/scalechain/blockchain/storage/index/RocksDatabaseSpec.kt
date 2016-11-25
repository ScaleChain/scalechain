package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
// Currently RocksDB crashes while seeking a key and iterating (key,value) pairs.
class RocksDatabaseSpec : KeyValueDatabaseTestTrait with KeyValueSeekTestTrait with KeyValuePrefixedSeekTestTrait with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null


  override fun beforeEach() {

    val testPath = File("./target/unittests-RocksDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = RocksDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }
}
