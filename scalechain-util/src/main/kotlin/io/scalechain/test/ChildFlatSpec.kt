package io.scalechain.test

import io.kotlintest.specs.FlatSpec

/**
 * A child FlatSpec mixed into a parent FlatSpec.
 * beforeEach and afterEach calls parent's beforeEach and afterEach respectively.
 */
open class ChildFlatSpec(private val parentSpec : BeforeAfterEach) : FlatSpec() {
  override fun beforeEach() {
    super.beforeEach()

    parentSpec.beforeEach()
  }

  override fun afterEach() {
    parentSpec.afterEach()

    super.afterEach()
  }
}