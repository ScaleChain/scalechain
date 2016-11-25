package io.scalechain.blockchain.storage.record

import io.scalechain.blockchain.storage.Storage
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockFileNameSpec : FlatSpec with BeforeAndAfterEach with Matchers {
  this: Suite =>

  Storage.initialize()

  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

  }

  "apply" should "return formatted file names" in {
    BlockFileName("blk", 0) shouldBe "blk00000.dat"
    BlockFileName("blk", 1) shouldBe "blk00001.dat"
    BlockFileName("blk", 99999) shouldBe "blk99999.dat"
    BlockFileName("blk", 100000) shouldBe "blk100000.dat"
  }

  "apply" should "hit an assertion if file number is less than 0" in {
    intercept<AssertionError> {
      BlockFileName("blk", -1)
    }
  }

  "apply" should "hit an assertion if the length of the prefix is not equal to 3" in {
    // prefix length is two : "bl"
    intercept<AssertionError> {
      BlockFileName("bl", 0)
    }

    // prefix length is four : "blkk"
    intercept<AssertionError> {
      BlockFileName("blkk", 0)
    }
  }

  fun extract(fileName: String) {
    fileName match {
      case BlockFileName(prefix, fileNumber) => Some(prefix, fileNumber)
      case _ => None
    }
  }

  "unapply" should "match correct file names" in {
    extract("blk00000.dat") shouldBe Some(("blk", 0))
    extract("blk00001.dat") shouldBe Some(("blk", 1))
    extract("blk99999.dat") shouldBe Some(("blk", 99999))
    extract("blk100000.dat") shouldBe Some(("blk", 100000))
  }

  "unapply" should "not match if the length of the prefix is greater than 3" in {
    // prefix length is four : "blkk"
    extract("blkk00000.dat") shouldBe None
  }

  "unapply" should "not match if the number part has non-numeric characters" in {
    extract("blka0000.dat") shouldBe None
    extract("blk0a000.dat") shouldBe None
    extract("blk00a00.dat") shouldBe None
    extract("blk000a0.dat") shouldBe None
    extract("blk0000a.dat") shouldBe None
    extract("blk0000#.dat") shouldBe None
  }

  "unapply" should "not match if the file name does not end with .dat" in {
    extract("blk00000") shouldBe None
    extract("blk00000.") shouldBe None
    extract("blk00000.d") shouldBe None
    extract("blk00000.da") shouldBe None
    extract("blk00000.daa") shouldBe None
  }

}
