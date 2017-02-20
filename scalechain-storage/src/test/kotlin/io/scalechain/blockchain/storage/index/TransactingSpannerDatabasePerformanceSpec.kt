package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactingSpannerDatabasePerformanceSpec : FlatSpec(), Matchers, KeyValueDatabasePerformanceTrait {
  lateinit var transactingDB : TransactingKeyValueDatabase
  lateinit override var db : KeyValueDatabase

  override fun beforeEach() {

    val spanner = SpannerDatabase(SpannerDatabaseSpec.INSTANCE_ID, SpannerDatabaseSpec.DATABASE_ID, "test")
    SpannerDatabaseSpec.truncateTable(spanner.getDbClient(), spanner.tableName)

    db = spanner

    transactingDB = db.transacting()
    transactingDB.beginTransaction()

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    transactingDB.commitTransaction()
    db.close()
  }

  init {
    Storage.initialize()
    addTests()
  }
}
