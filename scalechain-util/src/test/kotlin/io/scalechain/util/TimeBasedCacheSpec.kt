package io.scalechain.util

import io.kotlintest.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.*
import org.junit.runner.RunWith

import java.io.File
import java.util.concurrent.TimeUnit


@RunWith(KTestJUnitRunner::class)
class TimeBasedCacheSpec : FlatSpec(), Matchers {

    val testPath = File("./target/unittests-IncompleteBlockCacheSpec-storage/")

    var cache : TimeBasedCache<Int, String>? = null

    val CACHE_KEEP_MILLISECONDS = 50L

    override fun beforeEach() {
        // set-up code
        cache = TimeBasedCache<Int, String>(CACHE_KEEP_MILLISECONDS, TimeUnit.MILLISECONDS)

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
        cache = null
    }

    init {
        "getBlock" should "return None if no block/transaction was added" {
            cache?.get(1) shouldBe null
        }

        "getBlock" should "return an IncompleteBlock if a signing transaction was added" {
            cache?.put(1, "foo")

            cache?.get(1) shouldBe "foo"
        }

        "getBlock" should "return None if a signing transaction was added but expired" {
            cache?.put(1, "foo")

            Thread.sleep(CACHE_KEEP_MILLISECONDS + 10)

            cache?.get(1) shouldBe null
        }
    }
}