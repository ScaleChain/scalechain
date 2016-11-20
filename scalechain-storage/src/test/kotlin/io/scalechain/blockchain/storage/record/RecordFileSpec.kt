package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.{FileNumberCodec, CodecTestUtil}
import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RecordFileSpec : FlatSpec with BeforeAndAfterEach with Matchers with CodecTestUtil {
  this: Suite =>

  Storage.initialize()

  var file : RecordFile = null
  val MAX_SIZE = 64

  override fun beforeEach() {

    val f = File("./target/unittests-RecordFileSpec")
    if (f.exists())
      f.delete()

    file = RecordFile(f, MAX_SIZE)

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    file.close()
  }

  "appendRecord/readRecord" should "be able to append/read a data class instance" in {
    val record = FileNumber(1)
    val locator = file.appendRecord(record)(FileNumberCodec)
    file.readRecord(locator)(FileNumberCodec) shouldBe record
  }

  "appendRecord/readRecord" should "be able to append/read multiple data class instance" in {
    val record1 = FileNumber(1)
    val record2 = FileNumber(2)
    val record3 = FileNumber(3)

    val locator1 = file.appendRecord(record1)(FileNumberCodec)
    val locator2 = file.appendRecord(record2)(FileNumberCodec)
    val locator3 = file.appendRecord(record3)(FileNumberCodec)


    file.readRecord(locator1)(FileNumberCodec) shouldBe record1
    file.readRecord(locator2)(FileNumberCodec) shouldBe record2
    file.readRecord(locator3)(FileNumberCodec) shouldBe record3
  }

}
