package io.scalechain.blockchain.script

import org.scalatest.*

/** Test common super classes of script operations in ScriptOp.scala
  *
  */
class ScriptOpSpec : FlatSpec with BeforeAndAfterEach with OperationTestTrait {

  this: Suite =>

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

  "method" should "do something" {
  }
}
