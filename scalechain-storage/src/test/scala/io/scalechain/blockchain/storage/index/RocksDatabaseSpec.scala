package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RocksDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var db : RocksDatabase = null


  override def beforeEach() {

    val testPath = "./target/unittests-RocksDatabaseSpec"
    FileUtils.deleteDirectory(new File(testPath))
    db =  new RocksDatabase(testPath)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  "putObject(rawKey)/getObject(rawKey)" should "" in {
  }

  "putObject(objectKey)/getObject(objectKey)" should "" in {
  }

  "delObject(objectKey)" should "" in {
  }

  "get" should "" in {
  }

  "put" should "" in {
  }

  "del" should "" in {
  }
}
