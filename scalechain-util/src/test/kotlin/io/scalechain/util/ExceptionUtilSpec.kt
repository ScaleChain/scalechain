package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(KTestJUnitRunner::class)
class ExceptionUtilSpec : FlatSpec(), Matchers {

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
    "describe" should "return a string that contains the name of the thrown exception" {
      try {
        throw IllegalArgumentException()
      } catch ( t : Throwable ) {
        assertTrue( ExceptionUtil.describe(t).contains("IllegalArgumentException") )
      }
    }
    "describe" should "return an empty string if a null value is given" {
      ExceptionUtil.describe(null) shouldBe ""
    }
  }
}
