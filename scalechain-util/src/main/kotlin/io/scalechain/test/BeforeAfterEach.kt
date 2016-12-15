package io.scalechain.test

/** For a test suite, provide hooks called before and after each test.
 * This interface is required to call the parent test's beforeEach and afterEach method in the child test suite.
 */
interface BeforeAfterEach {
  fun beforeEach(): Unit
  fun afterEach(): Unit
}
