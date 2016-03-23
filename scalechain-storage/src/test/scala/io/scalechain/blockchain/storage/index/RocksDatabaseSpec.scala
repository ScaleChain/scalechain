package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto.{RecordLocator, FileNumber}
import io.scalechain.blockchain.proto.codec.{RecordLocatorCodec, FileNumberCodec}
import io.scalechain.blockchain.storage.Storage
import io.scalechain.crypto.HashFunctions
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RocksDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null


  override def beforeEach() {

    val testPath = new File("./target/unittests-RocksDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = new RocksDatabase( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }
}
