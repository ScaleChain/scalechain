package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
/*
@RunWith(KTestJUnitRunner::class)
class LevelDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {

  val testPath = File("./target/unittests-LevelDatabaseSpec")

  override lateinit var db : KeyValueDatabase

  override fun beforeEach() {
    super.beforeEach()

    testPath.deleteRecursively()
    testPath.mkdir()

    db = LevelDatabase( testPath )
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
  }

  init {
    Storage.initialize()
    addTests()
  }

}
*/

