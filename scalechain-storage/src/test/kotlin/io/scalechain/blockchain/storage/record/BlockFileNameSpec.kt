package io.scalechain.blockchain.storage.record

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.Storage

/**
  * Created by kangmo on 11/2/15.
  */
class BlockFileNameSpec : FlatSpec(), Matchers {
  init {
    Storage.initialize()

    "apply" should "return formatted file names" {
      BlockFileName("blk", 0) shouldBe "blk00000.dat"
      BlockFileName("blk", 1) shouldBe "blk00001.dat"
      BlockFileName("blk", 99999) shouldBe "blk99999.dat"
      BlockFileName("blk", 100000) shouldBe "blk100000.dat"
    }

    "apply" should "hit an assertion if file number is less than 0" {
      shouldThrow<AssertionError> {
        BlockFileName("blk", -1)
      }
    }

    "apply" should "hit an assertion if the length of the prefix is not equal to 3" {
      // prefix length is two : "bl"
      shouldThrow<AssertionError> {
        BlockFileName("bl", 0)
      }

      // prefix length is four : "blkk"
      shouldThrow<AssertionError> {
        BlockFileName("blkk", 0)
      }
    }

    fun extract(fileName: String) : Pair<String, Int>? {
      val blockFileName : BlockFileName? = BlockFileName.from(fileName)
      if (blockFileName != null) {
        return Pair(blockFileName.prefix, blockFileName.fileNumber)
      } else {
        return null
      }
    }

    "unapply" should "match correct file names" {
      extract("blk00000.dat") shouldBe Pair("blk", 0)
      extract("blk00001.dat") shouldBe Pair("blk", 1)
      extract("blk99999.dat") shouldBe Pair("blk", 99999)
      extract("blk100000.dat") shouldBe Pair("blk", 100000)
    }

    "unapply" should "not match if the length of the prefix is greater than 3" {
      // prefix length is four : "blkk"
      extract("blkk00000.dat") shouldBe null
    }

    "unapply" should "not match if the number part has non-numeric characters" {
      extract("blka0000.dat") shouldBe null
      extract("blk0a000.dat") shouldBe null
      extract("blk00a00.dat") shouldBe null
      extract("blk000a0.dat") shouldBe null
      extract("blk0000a.dat") shouldBe null
      extract("blk0000#.dat") shouldBe null
    }

    "unapply" should "not match if the file name does not end with .dat" {
      extract("blk00000") shouldBe null
      extract("blk00000.") shouldBe null
      extract("blk00000.d") shouldBe null
      extract("blk00000.da") shouldBe null
      extract("blk00000.daa") shouldBe null
    }
  }
}
