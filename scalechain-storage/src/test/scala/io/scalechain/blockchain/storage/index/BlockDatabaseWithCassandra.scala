package io.scalechain.blockchain.storage.index


import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
// Cassandara test is taking too long. Disable this test temporarily.
@Ignore
class BlockDatabaseWithCassandra extends BlockDatabaseTestTrait with BeforeAndAfterEach with BeforeAndAfterAll {
this: Suite =>
  Storage.initialize()

  var cassandraDatabase : CassandraDatabase = null
  var db : BlockDatabase = null

  val cassandraPath = "./target/embeddedCassandra-BlockDatabaseWithCassandra/"
  val testPath = new File(cassandraPath)

  override def beforeAll() {
    FileUtils.deleteDirectory( testPath )
    testPath.mkdir()

    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, cassandraPath);

    cassandraDatabase = new CassandraDatabase( "127.0.0.1", 9142, "kvstore" )
    db = new BlockDatabase( cassandraDatabase )

    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()
    db.close()

//    EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
  }

  override def beforeEach() {
    cassandraDatabase.truncateTable()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // Clean-up for each test cases.
  }
}
