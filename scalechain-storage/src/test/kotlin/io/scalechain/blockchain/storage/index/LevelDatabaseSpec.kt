package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
  * Created by kangmo on 11/2/15.
  */
// This test fails if it runs with sbt.
// https://github.com/eligosource/eventsourced/wiki/Installation#native
@Ignore
class LevelDatabaseSpec extends KeyValueDatabaseTestTrait with KeyValueSeekTestTrait with KeyValuePrefixedSeekTestTrait with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null


  override def beforeEach() {

    val testPath = new File("./target/unittests-LevelDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = new LevelDatabase( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }
}
