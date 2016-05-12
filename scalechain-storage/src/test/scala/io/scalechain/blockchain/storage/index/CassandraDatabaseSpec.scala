package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
// Cassandra is taking too long for the unit test. Disable it temporarily.
/*
class CassandraDatabaseSpec extends KeyValueDatabaseTestTrait with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null

  val cassandraPath= "./target/embeddedCassandra-CassandraDatabaseSpec/"
  val testPath = new File(cassandraPath)

  override def beforeAll() {
    FileUtils.deleteDirectory( testPath )
    testPath.mkdir()

    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, cassandraPath);

    db = new CassandraDatabase( testPath, "kvstore" )

    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()
    db.close()

//    EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
  }

  override def beforeEach() {
    db.asInstanceOf[CassandraDatabase].truncateTable()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // Clean-up for each test cases.
  }
}
*/