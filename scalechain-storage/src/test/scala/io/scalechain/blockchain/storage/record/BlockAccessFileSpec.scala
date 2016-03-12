package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.record.BlockAccessFile
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockAccessFileSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var file : BlockAccessFile = null
  val MAX_SIZE = 64

  override def beforeEach() {
    val f = new File("./target/unittests-BlockAccessFileSpec")
    if (f.exists())
      f.delete()

    file = new BlockAccessFile(f, MAX_SIZE)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    file.close()
  }

  "offset" should "" in {
  }

  "moveTo" should "" in {
  }

  "read" should "" in {
  }

  "write" should "" in {
  }

  "append" should "" in {
  }

  "flush" should "" in {
  }

}
