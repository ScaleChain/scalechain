package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class EitherSpec : FlatSpec(), Matchers {
  val LEFT_VALUE = "left"
  val RIGHT_VALUE = "right"
  val left = Either.Left(LEFT_VALUE)
  val right = Either.Right(RIGHT_VALUE)


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
    "Left.isLeft()" should "return true" {
      left.isLeft() shouldBe true
    }
    "Left.isRight()" should "return false" {
      left.isRight() shouldBe false
    }
    "Left.left()" should "return the left object" {
      left.left() shouldBe LEFT_VALUE
    }
    "Left.right()" should "return null" {
      (left.right() == null) shouldBe true
    }

    "Right.isLeft()" should "return false" {
      right.isLeft() shouldBe false
    }
    "Right.isRight()" should "return true" {
      right.isRight() shouldBe true
    }
    "Right.left()" should "return null" {
      (right.left() == null) shouldBe true
    }
    "Right.right()" should "return the right object" {
      right.right() shouldBe RIGHT_VALUE
    }
  }
}