package io.scalechain.blockchain.storage.record

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class RecordFileSpec : FlatSpec(), Matchers, CodecTestUtil {

  lateinit var file : RecordFile

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

  init {
    Storage.initialize()

    "appendRecord/readRecord" should "be able to append/read a data class instance" {
      val record = FileNumber(1)
      val locator = file.appendRecord(FileNumberCodec, record)
      file.readRecord(FileNumberCodec, locator) shouldBe record
    }

    "appendRecord/readRecord" should "be able to append/read multiple data class instance" {
      val record1 = FileNumber(1)
      val record2 = FileNumber(2)
      val record3 = FileNumber(3)

      val locator1 = file.appendRecord(FileNumberCodec, record1)
      val locator2 = file.appendRecord(FileNumberCodec, record2)
      val locator3 = file.appendRecord(FileNumberCodec, record3)

      file.readRecord(FileNumberCodec, locator1) shouldBe record1
      file.readRecord(FileNumberCodec, locator2) shouldBe record2
      file.readRecord(FileNumberCodec, locator3) shouldBe record3
    }
  }

  companion object {
    val MAX_SIZE = 64L
  }
}
