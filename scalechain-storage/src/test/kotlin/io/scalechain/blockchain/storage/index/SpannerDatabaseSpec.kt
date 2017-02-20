package io.scalechain.blockchain.storage.index

import com.google.cloud.spanner.DatabaseClient
import com.google.cloud.spanner.KeySet
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.Statement
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
 * Created by kangmo on 11/2/15.
 */
// Currently RocksDB crashes while seeking a key and iterating (key,value) pairs.
@RunWith(KTestJUnitRunner::class)
class SpannerDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {

  lateinit override var db : KeyValueDatabase

  override fun beforeEach()
  {
    val spanner = SpannerDatabase( INSTANCE_ID,  DATABASE_ID, TEST_TABLE_NAME)
    truncateTable( spanner.getDbClient(), spanner.tableName)

    db = spanner

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

  companion object {
    val INSTANCE_ID = "scalechain"
    val DATABASE_ID = "blockchain"
    val TEST_TABLE_NAME = "test"

    /**
     * Truncate a table for each run of our test.
     */
    fun truncateTable(dbClient : DatabaseClient, tableName : String ) : Unit {
      val mutations = mutableListOf<Mutation>()

      mutations.add(
        Mutation.delete(tableName, KeySet.all())
      )

      dbClient.write(mutations);
    }
  }
}
