package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ByteArrayExtSpec : FlatSpec(), Matchers {

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  init {
    "from" should "return a byte array that has the given byte" {
      ByteArrayExt.from(0.toByte()).toList() shouldBe byteArrayOf(0.toByte()).toList()
      ByteArrayExt.from(255.toByte()).toList() shouldBe byteArrayOf(255.toByte()).toList()
    }
  }
}
