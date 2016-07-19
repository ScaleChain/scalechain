package io.scalechain.util

import java.io.File
import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, Suite}

class TimeBasedCacheSpec extends FlatSpec with BeforeAndAfterEach with Matchers {

  this: Suite =>

  val testPath = new File("./target/unittests-IncompleteBlockCacheSpec-storage/")

  var cache : TimeBasedCache[String] = null

  val CACHE_KEEP_MILLISECONDS = 10

  override def beforeEach() {
    super.beforeEach()

    cache = new TimeBasedCache[String](CACHE_KEEP_MILLISECONDS, TimeUnit.MILLISECONDS)
  }

  override def afterEach() {
    super.afterEach()

    cache = null
  }

  "getBlock" should "return None if no block/transaction was added" in {
    val blockHash = data.Block.BLK02.header.hash
    cache.get(blockHash) shouldBe None
  }

  "getBlock" should "return an IncompleteBlock if a signing transaction was added" in {
    val blockHash = Hash

    cache.put(blockHash, data.Block.BLK02)

    cache.get(blockHash) shouldBe Some(data.Block.BLK02)
  }

  "getBlock" should "return None if a signing transaction was added but expired" in {
    val blockHash = data.Block.BLK02.header.hash

    cache.put(blockHash, data.Block.BLK02)

    Thread.sleep(CACHE_KEEP_MILLISECONDS + 10)

    cache.get(blockHash) shouldBe None
  }


}