package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

/**
 * Created by kangmo on 15/12/2016.
 */
@RunWith(KTestJUnitRunner::class)
class ListExtSpec : FlatSpec(), Matchers {
  init {
    "fill" should "fill zero elements" {
      ListExt.fill(0, 0).isEmpty() shouldBe true
    }

    "fill" should "fill one element" {
      ListExt.fill(1, 1.toByte()) shouldBe listOf(1.toByte())
    }

    "fill" should "fill two elements" {
      ListExt.fill(2, 1.toByte()) shouldBe listOf(1.toByte(), 1.toByte())
    }

  }
}