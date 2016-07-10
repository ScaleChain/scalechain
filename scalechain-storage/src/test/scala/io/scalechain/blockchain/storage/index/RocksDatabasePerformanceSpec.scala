package io.scalechain.blockchain.storage.index

import java.io.File
import java.math.BigInteger

import io.scalechain.blockchain.proto.{Transaction, Hash}
import io.scalechain.blockchain.storage.{TransactionPoolIndex, Storage}
import io.scalechain.crypto.HashFunctions
import io.scalechain.util.GlobalStopWatch
import org.apache.commons.io.FileUtils
import org.scalatest._

import scala.collection.mutable.ListBuffer
import scala.util.Random

class RocksDatabasePerformanceSpec extends FlatSpec with KeyValueDatabasePerformanceTrait with ShouldMatchers with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  var db : KeyValueDatabase = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-RocksDatabasePerformanceSpec")
    FileUtils.deleteDirectory( testPath )
    db = new RocksDatabase( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }
}
