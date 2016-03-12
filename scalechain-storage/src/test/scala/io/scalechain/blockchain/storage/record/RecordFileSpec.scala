package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RecordFileSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var file : RecordFile = null
  val MAX_SIZE = 64

  override def beforeEach() {

    val f = new File("./target/unittests-RecordFileSpec")
    if (f.exists())
      f.delete()

    file = new RecordFile(f, MAX_SIZE)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    file.close()
  }

  "readRecord" should "" in {
  }


  "appendRecord" should "" in {
  }

}
