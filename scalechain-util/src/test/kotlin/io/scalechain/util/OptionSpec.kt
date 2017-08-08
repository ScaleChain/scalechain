package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(KTestJUnitRunner::class)
class OptionSpec : FlatSpec(), Matchers {
  init {
    "Option.from(null)" should "return None" {
      Option.from<String>(null) shouldBe Option.None<String>()
    }

    "Option.from(string)" should "return Some(string)" {
      Option.from("") shouldBe Option.Some("")
      Option.from("test") shouldBe Option.Some("test")
    }
    "Some.toNullable" should "return the value" {
      Option.Some("").toNullable() shouldBe ""
      Option.Some("test").toNullable() shouldBe "test"
    }
    "Some.hashCode" should "" {
      Option.Some("").hashCode() shouldBe "".hashCode()
      Option.Some("test").hashCode() shouldBe "test".hashCode()
    }
    "Some.equals" should "return true if the other object is Some and has the same value" {
      Option.Some("").equals( Option.Some("") ) shouldBe true
      Option.Some("").equals( Option.Some("1") ) shouldBe false
      Option.Some("1").equals( Option.Some("") ) shouldBe false
      Option.Some("1").equals( Option.Some("1") ) shouldBe true
      Option.Some("1").equals( Option.None<String>() ) shouldBe false
    }

    "None.toNullable" should "return null" {
      assertTrue(Option.None<String>().toNullable() == null)
    }
    "None.hashCode" should "return the same value for two different types" {
      Option.None<String>().hashCode() shouldBe Option.None<Int>().hashCode()
    }
    "None.equals" should "return true if the other object is also None" {
      Option.None<String>().equals( Option.None<String>() ) shouldBe true
      Option.None<String>().equals( Option.None<Int>() ) shouldBe true
      Option.None<String>().equals( Option.Some("") ) shouldBe false
    }
  }
}