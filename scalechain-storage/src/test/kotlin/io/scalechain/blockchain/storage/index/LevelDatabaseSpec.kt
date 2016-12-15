package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import io.scalechain.test.BeforeAfterEach
import org.apache.commons.io.FileUtils
import org.junit.Ignore
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// This test fails if it runs with sbt.
// https://github.com/eligosource/eventsourced/wiki/Installation#native
@RunWith(KTestJUnitRunner::class)
class LevelDatabaseKeyValueSpec : FlatSpec(), Matchers, BeforeAfterEach, KeyValueDatabaseHolder, KeyValueDatabaseTestTrait, KeyValueSeekTestTrait, KeyValuePrefixedSeekTestTrait {

  val testPath = File("./target/unittests-LevelDatabaseSpec")

  override lateinit var db : KeyValueDatabase

  override fun beforeEach() {
    FileUtils.deleteDirectory( testPath )

    db = LevelDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
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


