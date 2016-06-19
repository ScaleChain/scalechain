package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.storage.index.CassandraDatabase
import io.scalechain.blockchain.storage.test.TestData
import org.apache.commons.io.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{Ignore, BeforeAndAfterAll, Suite, BeforeAndAfterEach}

// For Unit tests.
class CassandraBlockStorageForUnitTest(directoryPath : File) extends CassandraBlockStorage(directoryPath, "127.0.0.1", 9142 ) {
  protected[storage] def truncateTables(): Unit = {
    blocksTable.truncateTable
    transactionsTable.truncateTable
  }
}

// Cassandra Test is taking too long. Temporarily disable the suite.
class CassandraBlockStorageSpec extends BlockStorageTestTrait with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>


  import TestData._

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var cassandraBlockStorage: CassandraBlockStorageForUnitTest = null
  var storage: BlockStorage = null

  val cassandraPath = "./target/embeddedCassandra-CassandraBlockStorageSpec/"
  val rocksdbPath = "./target/embeddedCassandra-CassandraBlockStorageSpec/rocksdb/"

  val testPath = new File(cassandraPath)

  override def beforeAll() {
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, cassandraPath);

    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()

    cassandraBlockStorage.close()
    EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
  }

  val rocksDbTestPath = new File(rocksdbPath)
  override def beforeEach() {
    FileUtils.deleteDirectory(rocksDbTestPath)
    rocksDbTestPath.mkdir()

    cassandraBlockStorage = new CassandraBlockStorageForUnitTest(testPath)
    cassandraBlockStorage.truncateTables()
    storage = cassandraBlockStorage

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    cassandraBlockStorage.close()

    FileUtils.deleteDirectory(rocksDbTestPath)
  }

}
