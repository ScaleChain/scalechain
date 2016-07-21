package io.scalechain.util

import java.io.File
import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, Suite}

class TimeBasedCacheSpec extends FlatSpec with BeforeAndAfterEach with Matchers {

  this: Suite =>

  val testPath = new File("./target/unittests-IncompleteBlockCacheSpec-storage/")

  var cache : TimeBasedCache[Integer, String] = null

  val CACHE_KEEP_MILLISECONDS = 10

  override def beforeEach() {
    super.beforeEach()

    cache = new TimeBasedCache[Integer, String](CACHE_KEEP_MILLISECONDS, TimeUnit.MILLISECONDS)
  }

  override def afterEach() {
    super.afterEach()

    cache = null
  }

  "getBlock" should "return None if no block/transaction was added" in {
    cache.get(1) shouldBe None
  }

  "getBlock" should "return an IncompleteBlock if a signing transaction was added" in {
    cache.put(1, "foo")

    cache.get(1) shouldBe Some("foo")
  }

  "getBlock" should "return None if a signing transaction was added but expired" in {
    cache.put(1, "foo")

    Thread.sleep(CACHE_KEEP_MILLISECONDS + 10)

    cache.get(1) shouldBe None
  }


}