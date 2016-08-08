package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase, CassandraDatabase}
import io.scalechain.blockchain.storage.test.TestData
import org.apache.commons.io.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{Ignore, BeforeAndAfterAll, Suite, BeforeAndAfterEach}

// For Unit tests.
class CassandraBlockStorageForUnitTest(directoryPath : File)(implicit db : KeyValueDatabase) extends CassandraBlockStorage(directoryPath, "127.0.0.1", 9142 )(db) {
  protected[storage] def truncateTables(): Unit = {
    blocksTable.truncateTable
    transactionsTable.truncateTable
  }

  def rocksDB() : RocksDatabase = {
    keyValueDB
  }
}

// Cassandra Test is taking too long. Temporarily disable the suite.
@Ignore
class CassandraBlockStorageSpec extends BlockStorageTestTrait with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>


  import TestData._

  Storage.initialize()

  implicit var db : KeyValueDatabase = null

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var cassandraBlockStorage: CassandraBlockStorageForUnitTest = null
  var storage: BlockStorage = null

  val cassandraPath = "./target/embeddedCassandra-CassandraBlockStorageSpec/"
  val rocksDbPath = "./target/embeddedCassandra-CassandraBlockStorageSpec/rocksdb"

  val testPath = new File(cassandraPath)
  val rocksDbTestPath  = new File(rocksDbPath)
//  implicit var db : KeyValueDatabase = null

  override def beforeAll() {
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, cassandraPath);

    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()

    EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
  }

  override def beforeEach() {
    FileUtils.deleteDirectory(rocksDbTestPath)
    rocksDbTestPath.mkdir()

    cassandraBlockStorage = new CassandraBlockStorageForUnitTest(testPath)
    cassandraBlockStorage.truncateTables()
    storage = cassandraBlockStorage

    db = cassandraBlockStorage.rocksDB()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    cassandraBlockStorage.close()
    db = null
  }

}