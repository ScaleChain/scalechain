package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 23..
  */
class RocksDatabaseCFSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var db : RocksDatabase = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-RocksDatabaseCFSpec")
    FileUtils.deleteDirectory( testPath )
    db = new RocksDatabase( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  "openWithColumnFamily" should "return an instance of RocksDB" in {

  }

  "openWithColumnFamily" should "hit an assertion if an instance is already allocated" in {

  }

  "createColumnFamily" should "add new column family to existing column family list" in {

  }

  "createColumnFamily" should "hit an assertion if new column family is already exist" in {

  }

  "createColumnFamily" should "add more than 1,000,000 column family" in {

  }

  "dropColumnFamily" should "drop the column family" in {

  }

  "putObject(column family, key, value)" should "store data" in {

  }

  "putObject(column family, key, value)" should "overwrite an existing data" in {

  }

  "getObject(column family, key)" should "return a value" in {

  }

}
