package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.Storage
import org.junit.Ignore
import org.junit.runner.RunWith
import java.io.File

/**
 * Created by kangmo on 18/12/2016.
 */

@RunWith(KTestJUnitRunner::class)
@Ignore // MapDatabase is too slow. Will enable this case after optimizing MapDatabase.
class MapDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {

  val testPath = File("./target/unittests-MapDatabaseSpec")

  lateinit override var db : KeyValueDatabase

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    db = MapDatabase( testPath )

    super.beforeEach()
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
