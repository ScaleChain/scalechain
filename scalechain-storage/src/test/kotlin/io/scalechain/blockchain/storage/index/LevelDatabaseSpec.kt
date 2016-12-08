package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.junit.Ignore

/**
  * Created by kangmo on 11/2/15.
  */
// This test fails if it runs with sbt.
// https://github.com/eligosource/eventsourced/wiki/Installation#native
@Ignore
class LevelDatabaseKeyValueSpec : KeyValueDatabaseTestTrait() {

  init {
    Storage.initialize()
    runTests()
  }

  val testPath = File("./target/unittests-LevelDatabaseSpec")

  lateinit override var db : KeyValueDatabase


  override fun beforeEach() {
    FileUtils.deleteDirectory( testPath )

    db = LevelDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }
}

@Ignore
class LevelDatabaseKeyValueSeekSpec : KeyValueSeekTestTrait() {

  init {
    Storage.initialize()
    runTests()
  }

  val testPath = File("./target/unittests-LevelDatabaseSpec")

  lateinit override var db : KeyValueDatabase


  override fun beforeEach() {
    FileUtils.deleteDirectory( testPath )

    db = LevelDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }
}

@Ignore
class LevelDatabaseKeyValuePrefixedSeekSpec : KeyValuePrefixedSeekTestTrait() {

  init {
    Storage.initialize()
    runTests()
  }

  val testPath = File("./target/unittests-LevelDatabaseSpec")

  lateinit override var db : KeyValueDatabase


  override fun beforeEach() {
    FileUtils.deleteDirectory( testPath )

    db = LevelDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }
}
