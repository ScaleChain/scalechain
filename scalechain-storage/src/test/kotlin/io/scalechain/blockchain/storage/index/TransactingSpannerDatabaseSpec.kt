package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.junit.Ignore
import org.junit.runner.RunWith

@Ignore  // SpannerDatabase requires Spanner instance setup in Google Cloud Platform.
@RunWith(KTestJUnitRunner::class)
class TransactingSpannerDatabaseSpec : FlatSpec(), Matchers, DatabaseTestTraits {

  lateinit override var db : KeyValueDatabase
  lateinit var txDb : TransactingKeyValueDatabase

  override fun beforeEach() {

    val spanner = SpannerDatabase(SpannerDatabaseSpec.INSTANCE_ID, SpannerDatabaseSpec.DATABASE_ID, "test")
    SpannerDatabaseSpec.truncateTable(spanner.getDbClient(), spanner.tableName)

    db = spanner

    txDb = db.transacting()
    txDb.beginTransaction()

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    txDb.commitTransaction()
    db.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}