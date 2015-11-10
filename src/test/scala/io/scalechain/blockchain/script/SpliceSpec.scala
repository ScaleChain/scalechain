package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._


/** Test splice operations in Splice.scala
  *
  */
class SpliceSpec extends FlatSpec with BeforeAndAfterEach with OperationTestTrait {

  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  val operations =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.

      // OP_SIZE(0x82) : Calculate string length of top item and push the result
      // Before : in
      // After  : in size
      (stack(""),  OpSize(), stack("", 0)),
      (stack("a"),  OpSize(), stack("a", 1)),
      (stack("ab"),  OpSize(), stack("ab", 2)),
      (stack("a",""),  OpSize(), stack("a","", 0)),
      (stack("a","b"),  OpSize(), stack("a","b", 1)),
      (stack("a","bc"),  OpSize(), stack("a","bc", 2)),
      (stack(),           OpSize(), ErrorCode.NotEnoughInput)
    )

  "operations" should "run and push expected value on the stack." in {
    forAll(operations) { ( inputValues : Array[ScriptValue], operation : ScriptOp, expectation : AnyRef )  =>
      verifyOperation(inputValues, operation, expectation);
    }
  }
}
