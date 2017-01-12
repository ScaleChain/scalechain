package io.scalechain.blockchain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ExceptionsSpec : FlatSpec(), Matchers {

  init {
    "Exception classes" should "be able to create instances" {
      // Not sure creating test cases for construction of each data classes in a test suite.
    }
  }
}