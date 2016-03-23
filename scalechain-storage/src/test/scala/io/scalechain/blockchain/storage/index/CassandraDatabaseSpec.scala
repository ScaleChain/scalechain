package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.blockchain.proto.codec.RecordLocatorCodec
import io.scalechain.blockchain.proto.{RecordLocator, FileNumber}
import io.scalechain.blockchain.proto.codec.{RecordLocatorCodec, FileNumberCodec}
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.Storage
import io.scalechain.crypto.HashFunctions
import io.scalechain.crypto.HashFunctions
import org.apache.commons.io.FileUtils
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class CassandraDatabaseSpec extends KeyValueDatabaseTestTrait with BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null
  val testPath = new File("./target/unittests-CassandraDatabaseSpec")

  override def beforeAll() {
    FileUtils.deleteDirectory( testPath )

    EmbeddedCassandraServerHelper.startEmbeddedCassandra();

    db = new CassandraDatabase( testPath )

    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()
    db.close()
  }

  override def beforeEach() {
    //EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    //db = new CassandraDatabase( testPath )
    db.asInstanceOf[CassandraDatabase].truncateTable()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // Clean-up for each test cases.
  }

}
